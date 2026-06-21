package com.commutecarpool.service;

import com.commutecarpool.dto.auth.AuthResponse;
import com.commutecarpool.dto.auth.LoginRequest;
import com.commutecarpool.dto.auth.RegisterRequest;
import com.commutecarpool.dto.auth.UserResponse;
import com.commutecarpool.dto.auth.UserUpdateRequest;
import com.commutecarpool.entity.User;
import com.commutecarpool.exception.BusinessException;
import com.commutecarpool.repository.UserRepository;
import com.commutecarpool.security.JwtUtil;
import com.commutecarpool.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new BusinessException(400, "用户名已存在");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new BusinessException(400, "邮箱已存在");
        }
        User user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        userRepository.save(user);
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return new AuthResponse(token, user.getId(), user.getUsername(), user.getEmail());
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(401, "密码错误");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return new AuthResponse(token, user.getId(), user.getUsername(), user.getEmail());
    }

    public UserResponse getCurrentUser() {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        UserResponse response = new UserResponse();
        BeanUtils.copyProperties(user, response);
        return response;
    }

    public UserResponse updateCurrentUser(UserUpdateRequest req) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        if (req.getEmail() != null) {
            user.setEmail(req.getEmail());
        }
        if (req.getAvatar() != null) {
            user.setAvatar(req.getAvatar());
        }
        if (req.getPassword() != null) {
            user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        }
        userRepository.save(user);
        UserResponse response = new UserResponse();
        BeanUtils.copyProperties(user, response);
        return response;
    }
}
