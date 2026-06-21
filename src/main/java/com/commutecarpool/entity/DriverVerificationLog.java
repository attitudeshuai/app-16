package com.commutecarpool.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "driver_verification_logs")
public class DriverVerificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verification_id", nullable = false)
    private DriverVerification verification;

    @Column(nullable = false, name = "operator_id")
    private Long operatorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "old_status")
    private VerificationStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "new_status")
    private VerificationStatus newStatus;

    @Column(nullable = false, length = 500)
    private String remark;

    @Column(name = "operation_ip")
    private String operationIp;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
