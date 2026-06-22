package com.commutecarpool.dto.restriction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestrictionResponse {

    private Long id;

    private Long driverId;

    private String restrictionType;

    private String status;

    private String reason;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String appealStatus;

    private String appealMaterial;

    private String appealReason;

    private LocalDateTime appealSubmittedAt;

    private Long reviewerId;

    private String reviewRemark;

    private LocalDateTime reviewedAt;

    private List<RestrictionLogResponse> logs;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
