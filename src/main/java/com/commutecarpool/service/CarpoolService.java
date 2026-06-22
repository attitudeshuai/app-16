package com.commutecarpool.service;

import com.commutecarpool.dto.PageResponse;
import com.commutecarpool.dto.carpool.CarpoolRequest;
import com.commutecarpool.dto.carpool.CarpoolResponse;
import com.commutecarpool.dto.carpool.CarpoolStatusRequest;
import com.commutecarpool.dto.pricing.SuggestedPriceResponse;
import com.commutecarpool.entity.Carpool;
import com.commutecarpool.entity.CarpoolStatus;
import com.commutecarpool.entity.Route;
import com.commutecarpool.exception.BusinessException;
import com.commutecarpool.repository.CarpoolRepository;
import com.commutecarpool.repository.DriverVerificationRepository;
import com.commutecarpool.repository.RouteRepository;
import com.commutecarpool.security.SecurityUtils;
import com.commutecarpool.util.PageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CarpoolService {

    private final CarpoolRepository carpoolRepository;
    private final RouteRepository routeRepository;
    private final DriverVerificationRepository verificationRepository;
    private final PricingService pricingService;
    private final DriverCreditService driverCreditService;

    public PageResponse<CarpoolResponse> listCarpools(CarpoolStatus status, LocalDate startDate, LocalDate endDate, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        Page<Carpool> carpoolPage;
        if (status != null && startDate != null && endDate != null) {
            carpoolPage = carpoolRepository.findByStatusAndTripDateBetween(status, startDate, endDate, pageable);
        } else if (status != null) {
            carpoolPage = carpoolRepository.findByStatus(status, pageable);
        } else if (startDate != null && endDate != null) {
            carpoolPage = carpoolRepository.findByTripDateBetween(startDate, endDate, pageable);
        } else {
            carpoolPage = carpoolRepository.findAll(pageable);
        }
        List<CarpoolResponse> content = carpoolPage.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return PageUtils.toPageResponse(carpoolPage, content);
    }

    public SuggestedPriceResponse previewSuggestedPrice(Long routeId, LocalDate tripDate, LocalTime tripTime, Integer availableSeats) {
        return pricingService.calculateSuggestedPrice(routeId, tripDate, tripTime, availableSeats);
    }

    @Transactional
    public CarpoolResponse createCarpool(CarpoolRequest req) {
        Route route = routeRepository.findById(req.getRouteId())
                .orElseThrow(() -> new BusinessException(404, "路线不存在"));
        if (!route.getOwnerId().equals(req.getDriverId())) {
            throw new BusinessException(403, "无权操作");
        }
        if (!verificationRepository.isDriverVerified(req.getDriverId())) {
            throw new BusinessException(403, "请先完成实名认证后再创建行程");
        }
        if (driverCreditService.isDriverRestricted(req.getDriverId())) {
            throw new BusinessException(403, "您当前处于信用限制期，无法创建新行程");
        }
        Carpool carpool = new Carpool();
        BeanUtils.copyProperties(req, carpool);

        pricingService.ensureCarpoolPricing(carpool, route);

        carpoolRepository.save(carpool);
        return toResponse(carpool);
    }

    public CarpoolResponse getCarpool(Long id) {
        Carpool carpool = carpoolRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "拼车不存在"));
        return toResponse(carpool);
    }

    @Transactional
    public CarpoolResponse updateCarpool(Long id, CarpoolRequest req) {
        Long userId = SecurityUtils.getCurrentUserId();
        Carpool carpool = carpoolRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "拼车不存在"));
        if (!carpool.getDriverId().equals(userId)) {
            throw new BusinessException(403, "无权操作");
        }
        if (!verificationRepository.isDriverVerified(userId)) {
            throw new BusinessException(403, "请先完成实名认证后再修改行程");
        }

        Route route = routeRepository.findById(req.getRouteId())
                .orElseThrow(() -> new BusinessException(404, "路线不存在"));

        BeanUtils.copyProperties(req, carpool);
        carpool.setId(id);

        if (req.getFinalPricePerSeat() != null) {
            SuggestedPriceResponse suggested = pricingService.calculateSuggestedPrice(
                    route.getId(),
                    carpool.getTripDate(),
                    parseTime(route.getStartTime()),
                    carpool.getAvailableSeats()
            );
            pricingService.validatePriceWithinRange(req.getFinalPricePerSeat(), suggested.getSuggestedPrice());
            carpool.setSuggestedPricePerSeat(suggested.getSuggestedPrice());
            carpool.setFinalPricePerSeat(req.getFinalPricePerSeat());
        }

        carpoolRepository.save(carpool);
        return toResponse(carpool);
    }

    public void deleteCarpool(Long id) {
        Carpool carpool = carpoolRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "拼车不存在"));
        if (!carpool.getDriverId().equals(SecurityUtils.getCurrentUserId())) {
            throw new BusinessException(403, "无权操作");
        }
        carpoolRepository.delete(carpool);
    }

    public CarpoolResponse updateStatus(Long id, CarpoolStatusRequest req) {
        Carpool carpool = carpoolRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "拼车不存在"));
        if (!carpool.getDriverId().equals(SecurityUtils.getCurrentUserId())) {
            throw new BusinessException(403, "无权操作");
        }
        CarpoolStatus newStatus;
        try {
            newStatus = CarpoolStatus.valueOf(req.getStatus());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(400, "无效的状态");
        }
        if (!isValidTransition(carpool.getStatus(), newStatus)) {
            throw new BusinessException(400, "无效的状态转换");
        }
        carpool.setStatus(newStatus);
        carpoolRepository.save(carpool);
        return toResponse(carpool);
    }

    private boolean isValidTransition(CarpoolStatus current, CarpoolStatus target) {
        if (current == CarpoolStatus.OPEN) {
            return target == CarpoolStatus.FULL || target == CarpoolStatus.COMPLETED || target == CarpoolStatus.CANCELLED;
        }
        if (current == CarpoolStatus.FULL) {
            return target == CarpoolStatus.OPEN || target == CarpoolStatus.COMPLETED || target == CarpoolStatus.CANCELLED;
        }
        return false;
    }

    private LocalTime parseTime(String timeStr) {
        if (timeStr == null || timeStr.isBlank()) {
            return null;
        }
        try {
            return LocalTime.parse(timeStr);
        } catch (Exception e) {
            return null;
        }
    }

    private CarpoolResponse toResponse(Carpool carpool) {
        CarpoolResponse response = new CarpoolResponse();
        BeanUtils.copyProperties(carpool, response);
        response.setStatus(carpool.getStatus().name());
        return response;
    }
}
