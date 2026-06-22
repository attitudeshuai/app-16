package com.commutecarpool.controller;

import com.commutecarpool.dto.ApiResponse;
import com.commutecarpool.dto.PageResponse;
import com.commutecarpool.dto.restriction.RestrictionLogResponse;
import com.commutecarpool.dto.restriction.RestrictionResponse;
import com.commutecarpool.dto.restriction.RestrictionReviewAppealRequest;
import com.commutecarpool.dto.restriction.RestrictionSubmitAppealRequest;
import com.commutecarpool.service.DriverCreditService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/driver-credit")
@RequiredArgsConstructor
public class DriverCreditController {

    private final DriverCreditService driverCreditService;

    @GetMapping("/my-restriction")
    public ApiResponse<RestrictionResponse> getMyActiveRestriction() {
        RestrictionResponse response = driverCreditService.getMyActiveRestriction();
        return ApiResponse.success(response);
    }

    @GetMapping("/my-history")
    public ApiResponse<List<RestrictionResponse>> getMyRestrictionHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(driverCreditService.getMyRestrictionHistory(page, size));
    }

    @PostMapping("/restrictions/{id}/appeal")
    public ApiResponse<RestrictionResponse> submitAppeal(
            @PathVariable Long id,
            @RequestBody @Valid RestrictionSubmitAppealRequest request) {
        return ApiResponse.success(driverCreditService.submitAppeal(id, request));
    }

    @GetMapping("/admin/restrictions")
    public ApiResponse<PageResponse<RestrictionResponse>> listRestrictions(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(driverCreditService.listRestrictions(status, type, page, size));
    }

    @GetMapping("/admin/restrictions/{id}")
    public ApiResponse<RestrictionResponse> getRestriction(@PathVariable Long id) {
        return ApiResponse.success(driverCreditService.getRestriction(id));
    }

    @PostMapping("/admin/restrictions/{id}/review-appeal")
    public ApiResponse<RestrictionResponse> reviewAppeal(
            @PathVariable Long id,
            @RequestBody @Valid RestrictionReviewAppealRequest request) {
        return ApiResponse.success("审核完成", driverCreditService.reviewAppeal(id, request));
    }

    @GetMapping("/admin/restrictions/{id}/logs")
    public ApiResponse<List<RestrictionLogResponse>> getRestrictionLogs(@PathVariable Long id) {
        return ApiResponse.success(driverCreditService.getRestrictionLogs(id));
    }
}
