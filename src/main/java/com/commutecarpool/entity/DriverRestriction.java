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
@Table(name = "driver_restrictions")
public class DriverRestriction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "driver_id")
    private Long driverId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "restriction_type")
    private RestrictionType restrictionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RestrictionStatus status = RestrictionStatus.ACTIVE;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(nullable = false, name = "start_time")
    private LocalDateTime startTime;

    @Column(nullable = false, name = "end_time")
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "appeal_status")
    private AppealStatus appealStatus = AppealStatus.NONE;

    @Column(name = "appeal_material", length = 1000)
    private String appealMaterial;

    @Column(name = "appeal_reason", length = 500)
    private String appealReason;

    @Column(name = "appeal_submitted_at")
    private LocalDateTime appealSubmittedAt;

    @Column(name = "reviewer_id")
    private Long reviewerId;

    @Column(name = "review_remark", length = 500)
    private String reviewRemark;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @OneToMany(mappedBy = "restriction", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    private List<DriverRestrictionLog> logs = new ArrayList<>();

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

    public void addLog(DriverRestrictionLog log) {
        logs.add(log);
        log.setRestriction(this);
    }
}
