package com.commutecarpool.controller;

import com.commutecarpool.dto.ApiResponse;
import com.commutecarpool.dto.verification.VerificationResponse;
import com.commutecarpool.dto.verification.VerificationSubmitRequest;
import com.commutecarpool.service.DriverVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/driver/verification")
@RequiredArgsConstructor
public class DriverVerificationController {

    private final DriverVerificationService verificationService;

    @PostMapping
    public ApiResponse<VerificationResponse> submitVerification(@RequestBody @Valid VerificationSubmitRequest request) {
        return ApiResponse.success("实名认证申请已提交", verificationService.submitVerification(request));
    }

    @GetMapping
    public ApiResponse<VerificationResponse> getMyVerification() {
        return ApiResponse.success(verificationService.getMyVerification());
    }

    @GetMapping("/status")
    public ApiResponse<Boolean> isVerified() {
        return ApiResponse.success(verificationService.isCurrentUserVerified());
    }
}
