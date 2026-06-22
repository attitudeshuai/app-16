package com.commutecarpool.dto.pricing;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SuggestedPriceResponse {

    private BigDecimal suggestedPrice;
    private BigDecimal minAllowedPrice;
    private BigDecimal maxAllowedPrice;
    private BigDecimal basePrice;
    private BigDecimal distanceMultiplier;
    private BigDecimal peakHourMultiplier;
    private BigDecimal heatMultiplier;
    private BigDecimal seatScarcityMultiplier;
    private BigDecimal temporaryRuleMultiplier;
    private String calculationDetail;
    private boolean isPeakHour;
    private long routeActiveCarpools;
    private long routeRecentBookings;
    private int remainingSeats;
    private String appliedRuleName;
    private BigDecimal driverAdjustmentRatio;
}
