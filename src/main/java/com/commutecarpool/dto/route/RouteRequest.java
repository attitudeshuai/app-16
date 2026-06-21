package com.commutecarpool.dto.route;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteRequest {

    @NotBlank
    private String startLocation;

    @NotBlank
    private String endLocation;

    private String startTime;

    private String returnTime;

    private String daysOfWeek;

    @NotNull
    @Min(1)
    private Integer seats;

    private BigDecimal pricePerSeat;

    private Boolean isActive;
}
