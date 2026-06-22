package com.commutecarpool.dto.pricing;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PricingConfigRequest {

    @NotNull
    @Positive
    private BigDecimal basePricePerKm;

    @NotNull
    @DecimalMin("0.5")
    @DecimalMax("3.0")
    private BigDecimal peakHourMultiplier;

    @NotNull
    @DecimalMin("0.5")
    @DecimalMax("3.0")
    private BigDecimal offPeakMultiplier;

    @NotNull
    @DecimalMin("0.5")
    @DecimalMax("3.0")
    private BigDecimal heatMultiplierBase;

    @NotNull
    @DecimalMin("0")
    @DecimalMax("0.5")
    private BigDecimal heatMultiplierPerBooking;

    @NotNull
    @DecimalMin("1.0")
    @DecimalMax("3.0")
    private BigDecimal heatMultiplierMax;

    @NotNull
    @PositiveOrZero
    private Integer seatScarcityThreshold;

    @NotNull
    @DecimalMin("1.0")
    @DecimalMax("3.0")
    private BigDecimal seatScarcityMultiplier;

    @NotNull
    @DecimalMin("0.5")
    @DecimalMax("2.0")
    private BigDecimal distanceWeight;

    @NotNull
    @Positive
    private BigDecimal minPricePerSeat;

    @NotNull
    @Positive
    private BigDecimal maxPricePerSeat;

    @NotNull
    @DecimalMin("0")
    @DecimalMax("0.5")
    private BigDecimal driverAdjustmentRatio;
}
