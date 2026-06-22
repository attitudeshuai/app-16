package com.commutecarpool.controller;

import com.commutecarpool.dto.ApiResponse;
import com.commutecarpool.dto.PageResponse;
import com.commutecarpool.dto.carpool.CarpoolRequest;
import com.commutecarpool.dto.carpool.CarpoolResponse;
import com.commutecarpool.dto.carpool.CarpoolStatusRequest;
import com.commutecarpool.dto.pricing.SuggestedPriceResponse;
import com.commutecarpool.entity.CarpoolStatus;
import com.commutecarpool.service.CarpoolService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/carpools")
@RequiredArgsConstructor
public class CarpoolController {

    private final CarpoolService carpoolService;

    @GetMapping
    public ApiResponse<PageResponse<CarpoolResponse>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        CarpoolStatus statusEnum = status != null ? CarpoolStatus.valueOf(status) : null;
        return ApiResponse.success(carpoolService.listCarpools(statusEnum, startDate, endDate, page, size));
    }

    @GetMapping("/suggested-price")
    public ApiResponse<SuggestedPriceResponse> previewSuggestedPrice(
            @RequestParam Long routeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tripDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "HH:mm") LocalTime tripTime,
            @RequestParam(defaultValue = "3") Integer availableSeats) {
        return ApiResponse.success(carpoolService.previewSuggestedPrice(routeId, tripDate, tripTime, availableSeats));
    }

    @PostMapping
    public ApiResponse<CarpoolResponse> create(@RequestBody @Valid CarpoolRequest request) {
        return ApiResponse.success(carpoolService.createCarpool(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<CarpoolResponse> get(@PathVariable Long id) {
        return ApiResponse.success(carpoolService.getCarpool(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<CarpoolResponse> update(@PathVariable Long id, @RequestBody @Valid CarpoolRequest request) {
        return ApiResponse.success(carpoolService.updateCarpool(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        carpoolService.deleteCarpool(id);
        return ApiResponse.success("删除成功", null);
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<CarpoolResponse> updateStatus(@PathVariable Long id, @RequestBody @Valid CarpoolStatusRequest request) {
        return ApiResponse.success(carpoolService.updateStatus(id, request));
    }
}
