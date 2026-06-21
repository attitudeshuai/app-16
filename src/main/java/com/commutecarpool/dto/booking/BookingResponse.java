package com.commutecarpool.dto.booking;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {

    private Long id;
    private Long carpoolId;
    private Long passengerId;
    private Integer seatsBooked;
    private String status;
    private LocalDateTime createdAt;
}
