package com.commutecarpool.controller;

import com.commutecarpool.dto.ApiResponse;
import com.commutecarpool.dto.PageResponse;
import com.commutecarpool.dto.verification.VerificationLogResponse;
import com.commutecarpool.dto.verification.VerificationResponse;
import com.commutecarpool.dto.verification.VerificationReviewRequest;
import com.commutecarpool.service.DriverVerificationService;
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
@RequestMapping("/api/admin/verifications")
@RequiredArgsConstructor
public class AdminVerificationController {

    private final DriverVerificationService verificationService;

    @GetMapping
    public ApiResponse<PageResponse<VerificationResponse>> list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(verificationService.listVerifications(status, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<VerificationResponse> get(@PathVariable Long id) {
        return ApiResponse.success(verificationService.getVerification(id));
    }

    @PostMapping("/{id}/review")
    public ApiResponse<VerificationResponse> review(
            @PathVariable Long id,
            @RequestBody @Valid VerificationReviewRequest request) {
        return ApiResponse.success("审核完成", verificationService.reviewVerification(id, request));
    }

    @GetMapping("/{id}/logs")
    public ApiResponse<List<VerificationLogResponse>> getAuditLogs(@PathVariable Long id) {
        return ApiResponse.success(verificationService.getAuditLogs(id));
    }
}
