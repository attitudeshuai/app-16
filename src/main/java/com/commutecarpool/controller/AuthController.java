package com.commutecarpool.controller;

import com.commutecarpool.dto.ApiResponse;
import com.commutecarpool.dto.auth.AuthResponse;
import com.commutecarpool.dto.auth.LoginRequest;
import com.commutecarpool.dto.auth.RegisterRequest;
import com.commutecarpool.dto.auth.UserUpdateRequest;
import com.commutecarpool.dto.auth.UserResponse;
import com.commutecarpool.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@RequestBody @Valid RegisterRequest request) {
        return ApiResponse.success(authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> me() {
        return ApiResponse.success(authService.getCurrentUser());
    }

    @PutMapping("/me")
    public ApiResponse<UserResponse> updateMe(@RequestBody @Valid UserUpdateRequest request) {
        return ApiResponse.success(authService.updateCurrentUser(request));
    }
}
