package com.commutecarpool.dto.carpool;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarpoolRequest {

    @NotNull
    private Long routeId;

    @NotNull
    private Long driverId;

    @NotNull
    private LocalDate tripDate;

    @NotNull
    @Min(1)
    private Integer availableSeats;

    private String note;
}
