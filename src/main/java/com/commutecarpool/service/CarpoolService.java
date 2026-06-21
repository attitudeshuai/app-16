package com.commutecarpool.service;

import com.commutecarpool.dto.PageResponse;
import com.commutecarpool.dto.carpool.CarpoolRequest;
import com.commutecarpool.dto.carpool.CarpoolResponse;
import com.commutecarpool.dto.carpool.CarpoolStatusRequest;
import com.commutecarpool.entity.Carpool;
import com.commutecarpool.entity.CarpoolStatus;
import com.commutecarpool.entity.Route;
import com.commutecarpool.exception.BusinessException;
import com.commutecarpool.repository.CarpoolRepository;
import com.commutecarpool.repository.RouteRepository;
import com.commutecarpool.security.SecurityUtils;
import com.commutecarpool.util.PageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CarpoolService {

    private final CarpoolRepository carpoolRepository;
    private final RouteRepository routeRepository;

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

    public CarpoolResponse createCarpool(CarpoolRequest req) {
        Route route = routeRepository.findById(req.getRouteId())
                .orElseThrow(() -> new BusinessException(404, "路线不存在"));
        if (!route.getOwnerId().equals(req.getDriverId())) {
            throw new BusinessException(403, "无权操作");
        }
        Carpool carpool = new Carpool();
        BeanUtils.copyProperties(req, carpool);
        carpoolRepository.save(carpool);
        return toResponse(carpool);
    }

    public CarpoolResponse getCarpool(Long id) {
        Carpool carpool = carpoolRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "拼车不存在"));
        return toResponse(carpool);
    }

    public CarpoolResponse updateCarpool(Long id, CarpoolRequest req) {
        Carpool carpool = carpoolRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "拼车不存在"));
        if (!carpool.getDriverId().equals(SecurityUtils.getCurrentUserId())) {
            throw new BusinessException(403, "无权操作");
        }
        BeanUtils.copyProperties(req, carpool);
        carpool.setId(id);
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

    private CarpoolResponse toResponse(Carpool carpool) {
        CarpoolResponse response = new CarpoolResponse();
        BeanUtils.copyProperties(carpool, response);
        response.setStatus(carpool.getStatus().name());
        return response;
    }
}
