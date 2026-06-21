package com.commutecarpool.service;

import com.commutecarpool.dto.PageResponse;
import com.commutecarpool.dto.verification.VerificationLogResponse;
import com.commutecarpool.dto.verification.VerificationResponse;
import com.commutecarpool.dto.verification.VerificationReviewRequest;
import com.commutecarpool.dto.verification.VerificationSubmitRequest;
import com.commutecarpool.entity.DriverVerification;
import com.commutecarpool.entity.DriverVerificationLog;
import com.commutecarpool.entity.User;
import com.commutecarpool.entity.VerificationStatus;
import com.commutecarpool.exception.BusinessException;
import com.commutecarpool.repository.DriverVerificationRepository;
import com.commutecarpool.repository.UserRepository;
import com.commutecarpool.security.SecurityUtils;
import com.commutecarpool.util.PageUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DriverVerificationService {

    private final DriverVerificationRepository verificationRepository;
    private final UserRepository userRepository;
    private final HttpServletRequest request;

    @Transactional
    public VerificationResponse submitVerification(VerificationSubmitRequest req) {
        Long driverId = SecurityUtils.getCurrentUserId();
        if (driverId == null) {
            throw new BusinessException(401, "未登录");
        }

        if (verificationRepository.existsByDriverIdAndStatus(driverId, VerificationStatus.PENDING)) {
            throw new BusinessException(400, "您已有正在审核中的实名认证申请");
        }

        DriverVerification verification = new DriverVerification();
        BeanUtils.copyProperties(req, verification);
        verification.setDriverId(driverId);
        verification.setApplicationNo(generateApplicationNo());
        verification.setStatus(VerificationStatus.PENDING);

        verificationRepository.save(verification);

        DriverVerificationLog log = createLog(
                verification,
                null,
                VerificationStatus.PENDING,
                "司机提交实名认证申请",
                driverId
        );
        verification.addLog(log);
        verificationRepository.save(verification);

        return toResponse(verification);
    }

    @Transactional
    public VerificationResponse reviewVerification(Long id, VerificationReviewRequest req) {
        if (!SecurityUtils.isAdmin()) {
            throw new BusinessException(403, "无权限执行此操作");
        }

        Long reviewerId = SecurityUtils.getCurrentUserId();
        DriverVerification verification = verificationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "审核记录不存在"));

        if (verification.getStatus() != VerificationStatus.PENDING) {
            throw new BusinessException(400, "该申请已审核，无法重复操作");
        }

        VerificationStatus newStatus;
        try {
            newStatus = VerificationStatus.valueOf(req.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(400, "无效的审核状态");
        }

        if (newStatus != VerificationStatus.APPROVED && newStatus != VerificationStatus.REJECTED) {
            throw new BusinessException(400, "审核状态必须为 APPROVED 或 REJECTED");
        }

        VerificationStatus oldStatus = verification.getStatus();
        verification.setStatus(newStatus);
        verification.setReviewerId(reviewerId);
        verification.setReviewRemark(req.getRemark());
        verification.setReviewedAt(LocalDateTime.now());

        DriverVerificationLog log = createLog(
                verification,
                oldStatus,
                newStatus,
                req.getRemark(),
                reviewerId
        );
        verification.addLog(log);

        if (newStatus == VerificationStatus.APPROVED) {
            User driver = userRepository.findById(verification.getDriverId())
                    .orElseThrow(() -> new BusinessException(404, "用户不存在"));
            driver.setRealNameVerified(true);
            userRepository.save(driver);
        }

        verificationRepository.save(verification);

        return toResponse(verification);
    }

    public PageResponse<VerificationResponse> listVerifications(String status, int page, int size) {
        if (!SecurityUtils.isAdmin()) {
            throw new BusinessException(403, "无权限执行此操作");
        }

        PageRequest pageable = PageRequest.of(page, size);
        Page<DriverVerification> verificationPage;

        if (status != null && !status.isEmpty()) {
            try {
                VerificationStatus statusEnum = VerificationStatus.valueOf(status.toUpperCase());
                verificationPage = verificationRepository.findByStatus(statusEnum, pageable);
            } catch (IllegalArgumentException e) {
                throw new BusinessException(400, "无效的状态值");
            }
        } else {
            verificationPage = verificationRepository.findAll(pageable);
        }

        List<VerificationResponse> content = verificationPage.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return PageUtils.toPageResponse(verificationPage, content);
    }

    public VerificationResponse getMyVerification() {
        Long driverId = SecurityUtils.getCurrentUserId();
        if (driverId == null) {
            throw new BusinessException(401, "未登录");
        }

        DriverVerification verification = verificationRepository.findByDriverId(driverId)
                .orElseThrow(() -> new BusinessException(404, "您还没有提交实名认证申请"));

        return toResponse(verification);
    }

    public VerificationResponse getVerification(Long id) {
        if (!SecurityUtils.isAdmin()) {
            throw new BusinessException(403, "无权限执行此操作");
        }

        DriverVerification verification = verificationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "审核记录不存在"));

        return toResponse(verification);
    }

    public List<VerificationLogResponse> getAuditLogs(Long id) {
        if (!SecurityUtils.isAdmin()) {
            throw new BusinessException(403, "无权限执行此操作");
        }

        DriverVerification verification = verificationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "审核记录不存在"));

        return verification.getAuditLogs().stream()
                .map(this::toLogResponse)
                .collect(Collectors.toList());
    }

    public boolean isCurrentUserVerified() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return false;
        }
        return verificationRepository.isDriverVerified(userId);
    }

    public boolean isDriverVerified(Long driverId) {
        return verificationRepository.isDriverVerified(driverId);
    }

    private String generateApplicationNo() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomPart = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "VR" + datePart + randomPart;
    }

    private DriverVerificationLog createLog(DriverVerification verification,
                                            VerificationStatus oldStatus,
                                            VerificationStatus newStatus,
                                            String remark,
                                            Long operatorId) {
        DriverVerificationLog log = new DriverVerificationLog();
        log.setVerification(verification);
        log.setOldStatus(oldStatus);
        log.setNewStatus(newStatus);
        log.setRemark(remark);
        log.setOperatorId(operatorId);
        log.setOperationIp(getClientIp());
        return log;
    }

    private String getClientIp() {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    private VerificationResponse toResponse(DriverVerification verification) {
        VerificationResponse response = new VerificationResponse();
        BeanUtils.copyProperties(verification, response);
        response.setStatus(verification.getStatus().name());
        if (verification.getAuditLogs() != null) {
            response.setAuditLogs(verification.getAuditLogs().stream()
                    .map(this::toLogResponse)
                    .collect(Collectors.toList()));
        }
        return response;
    }

    private VerificationLogResponse toLogResponse(DriverVerificationLog log) {
        VerificationLogResponse response = new VerificationLogResponse();
        BeanUtils.copyProperties(log, response);
        response.setOldStatus(log.getOldStatus() != null ? log.getOldStatus().name() : null);
        response.setNewStatus(log.getNewStatus().name());
        return response;
    }
}
