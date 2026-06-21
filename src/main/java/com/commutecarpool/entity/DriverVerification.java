package com.commutecarpool.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "driver_verifications")
public class DriverVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "driver_id")
    private Long driverId;

    @Column(nullable = false, unique = true, name = "application_no")
    private String applicationNo;

    @Column(nullable = false, name = "id_card_front")
    private String idCardFront;

    @Column(nullable = false, name = "id_card_back")
    private String idCardBack;

    @Column(nullable = false, name = "driving_license_front")
    private String drivingLicenseFront;

    @Column(nullable = false, name = "driving_license_back")
    private String drivingLicenseBack;

    @Column(nullable = false, name = "real_name")
    private String realName;

    @Column(nullable = false, name = "id_card_number")
    private String idCardNumber;

    @Column(nullable = false, name = "driving_license_number")
    private String drivingLicenseNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationStatus status = VerificationStatus.PENDING;

    @Column(name = "reviewer_id")
    private Long reviewerId;

    @Column(name = "review_remark", length = 500)
    private String reviewRemark;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @OneToMany(mappedBy = "verification", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    private List<DriverVerificationLog> auditLogs = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    private void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void addLog(DriverVerificationLog log) {
        auditLogs.add(log);
        log.setVerification(this);
    }
}
