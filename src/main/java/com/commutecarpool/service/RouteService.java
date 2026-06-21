package com.commutecarpool.service;

import com.commutecarpool.dto.PageResponse;
import com.commutecarpool.dto.route.RouteRequest;
import com.commutecarpool.dto.route.RouteResponse;
import com.commutecarpool.entity.Route;
import com.commutecarpool.exception.BusinessException;
import com.commutecarpool.repository.DriverVerificationRepository;
import com.commutecarpool.repository.RouteRepository;
import com.commutecarpool.security.SecurityUtils;
import com.commutecarpool.util.PageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RouteService {

    private final RouteRepository routeRepository;
    private final DriverVerificationRepository verificationRepository;

    public PageResponse<RouteResponse> listRoutes(String search, Boolean activeOnly, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        Page<Route> routePage;
        if (search != null && activeOnly != null && activeOnly) {
            routePage = routeRepository.findByIsActiveTrueAndStartLocationContainingOrEndLocationContaining(search, search, pageable);
        } else if (search != null) {
            routePage = routeRepository.findByStartLocationContainingOrEndLocationContaining(search, search, pageable);
        } else if (activeOnly != null && activeOnly) {
            routePage = routeRepository.findByIsActiveTrue(pageable);
        } else {
            routePage = routeRepository.findAll(pageable);
        }
        return PageUtils.toPageResponse(routePage, RouteResponse.class);
    }

    public RouteResponse createRoute(RouteRequest req) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (!verificationRepository.isDriverVerified(userId)) {
            throw new BusinessException(403, "请先完成实名认证后再发布路线");
        }
        Route route = new Route();
        BeanUtils.copyProperties(req, route);
        route.setOwnerId(userId);
        if (route.getIsActive() == null) {
            route.setIsActive(true);
        }
        routeRepository.save(route);
        RouteResponse response = new RouteResponse();
        BeanUtils.copyProperties(route, response);
        return response;
    }

    public RouteResponse getRoute(Long id) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "路线不存在"));
        RouteResponse response = new RouteResponse();
        BeanUtils.copyProperties(route, response);
        return response;
    }

    public RouteResponse updateRoute(Long id, RouteRequest req) {
        Long userId = SecurityUtils.getCurrentUserId();
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "路线不存在"));
        if (!route.getOwnerId().equals(userId)) {
            throw new BusinessException(403, "无权操作");
        }
        if (!verificationRepository.isDriverVerified(userId)) {
            throw new BusinessException(403, "请先完成实名认证后再修改路线");
        }
        BeanUtils.copyProperties(req, route);
        route.setId(id);
        if (route.getIsActive() == null) {
            route.setIsActive(true);
        }
        routeRepository.save(route);
        RouteResponse response = new RouteResponse();
        BeanUtils.copyProperties(route, response);
        return response;
    }

    public void deleteRoute(Long id) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "路线不存在"));
        if (!route.getOwnerId().equals(SecurityUtils.getCurrentUserId())) {
            throw new BusinessException(403, "无权操作");
        }
        routeRepository.delete(route);
    }

    public PageResponse<RouteResponse> getMyRoutes(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        Page<Route> routePage = routeRepository.findByOwnerId(SecurityUtils.getCurrentUserId(), pageable);
        return PageUtils.toPageResponse(routePage, RouteResponse.class);
    }
}
