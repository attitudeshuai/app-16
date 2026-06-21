package com.commutecarpool.dto.verification;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VerificationLogResponse {

    private Long id;
    private Long operatorId;
    private String oldStatus;
    private String newStatus;
    private String remark;
    private String operationIp;
    private LocalDateTime createdAt;
}
