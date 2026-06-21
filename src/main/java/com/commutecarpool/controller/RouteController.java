package com.commutecarpool.controller;

import com.commutecarpool.dto.ApiResponse;
import com.commutecarpool.dto.PageResponse;
import com.commutecarpool.dto.route.RouteRequest;
import com.commutecarpool.dto.route.RouteResponse;
import com.commutecarpool.service.RouteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;

    @GetMapping
    public ApiResponse<PageResponse<RouteResponse>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean activeOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(routeService.listRoutes(search, activeOnly, page, size));
    }

    @PostMapping
    public ApiResponse<RouteResponse> create(@RequestBody @Valid RouteRequest request) {
        return ApiResponse.success(routeService.createRoute(request));
    }

    @GetMapping("/mine")
    public ApiResponse<PageResponse<RouteResponse>> mine(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(routeService.getMyRoutes(page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<RouteResponse> get(@PathVariable Long id) {
        return ApiResponse.success(routeService.getRoute(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<RouteResponse> update(@PathVariable Long id, @RequestBody @Valid RouteRequest request) {
        return ApiResponse.success(routeService.updateRoute(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        routeService.deleteRoute(id);
        return ApiResponse.success("删除成功", null);
    }
}
