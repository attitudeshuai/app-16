package com.commutecarpool.dto.verification;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class VerificationResponse {

    private Long id;
    private Long driverId;
    private String applicationNo;
    private String idCardFront;
    private String idCardBack;
    private String drivingLicenseFront;
    private String drivingLicenseBack;
    private String realName;
    private String idCardNumber;
    private String drivingLicenseNumber;
    private String status;
    private Long reviewerId;
    private String reviewRemark;
    private LocalDateTime reviewedAt;
    private List<VerificationLogResponse> auditLogs;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
