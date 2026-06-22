package com.commutecarpool.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "pricing_configs")
public class PricingConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "base_price_per_km", precision = 10, scale = 2)
    private BigDecimal basePricePerKm = BigDecimal.valueOf(2.5);

    @Column(nullable = false, name = "peak_hour_multiplier", precision = 5, scale = 2)
    private BigDecimal peakHourMultiplier = BigDecimal.valueOf(1.3);

    @Column(nullable = false, name = "off_peak_multiplier", precision = 5, scale = 2)
    private BigDecimal offPeakMultiplier = BigDecimal.valueOf(0.9);

    @Column(nullable = false, name = "heat_multiplier_base", precision = 5, scale = 2)
    private BigDecimal heatMultiplierBase = BigDecimal.valueOf(1.0);

    @Column(nullable = false, name = "heat_multiplier_per_booking", precision = 5, scale = 4)
    private BigDecimal heatMultiplierPerBooking = BigDecimal.valueOf(0.02);

    @Column(nullable = false, name = "heat_multiplier_max", precision = 5, scale = 2)
    private BigDecimal heatMultiplierMax = BigDecimal.valueOf(1.5);

    @Column(nullable = false, name = "seat_scarcity_threshold")
    private Integer seatScarcityThreshold = 1;

    @Column(nullable = false, name = "seat_scarcity_multiplier", precision = 5, scale = 2)
    private BigDecimal seatScarcityMultiplier = BigDecimal.valueOf(1.15);

    @Column(nullable = false, name = "distance_weight", precision = 5, scale = 2)
    private BigDecimal distanceWeight = BigDecimal.valueOf(1.0);

    @Column(nullable = false, name = "min_price_per_seat", precision = 10, scale = 2)
    private BigDecimal minPricePerSeat = BigDecimal.valueOf(5.0);

    @Column(nullable = false, name = "max_price_per_seat", precision = 10, scale = 2)
    private BigDecimal maxPricePerSeat = BigDecimal.valueOf(100.0);

    @Column(nullable = false, name = "driver_adjustment_ratio", precision = 5, scale = 2)
    private BigDecimal driverAdjustmentRatio = BigDecimal.valueOf(0.2);

    @Column(name = "config_key", unique = true)
    private String configKey = "DEFAULT";

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
}
