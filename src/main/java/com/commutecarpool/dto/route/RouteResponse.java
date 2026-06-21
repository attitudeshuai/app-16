package com.commutecarpool.dto.route;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteResponse {

    private Long id;
    private Long ownerId;
    private String startLocation;
    private String endLocation;
    private String startTime;
    private String returnTime;
    private String daysOfWeek;
    private Integer seats;
    private BigDecimal pricePerSeat;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
