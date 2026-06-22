package com.commutecarpool.dto.pricing;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PricingConfigResponse {

    private Long id;
    private BigDecimal basePricePerKm;
    private BigDecimal peakHourMultiplier;
    private BigDecimal offPeakMultiplier;
    private BigDecimal heatMultiplierBase;
    private BigDecimal heatMultiplierPerBooking;
    private BigDecimal heatMultiplierMax;
    private Integer seatScarcityThreshold;
    private BigDecimal seatScarcityMultiplier;
    private BigDecimal distanceWeight;
    private BigDecimal minPricePerSeat;
    private BigDecimal maxPricePerSeat;
    private BigDecimal driverAdjustmentRatio;
    private String configKey;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
