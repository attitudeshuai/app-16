package com.commutecarpool.dto.carpool;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarpoolResponse {

    private Long id;
    private Long routeId;
    private Long driverId;
    private LocalDate tripDate;
    private Integer availableSeats;
    private String status;
    private String note;
    private BigDecimal suggestedPricePerSeat;
    private BigDecimal finalPricePerSeat;
    private LocalDateTime createdAt;
}
