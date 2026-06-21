package com.commutecarpool.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatsTrend {

    private String date;
    private long carpools;
    private long bookings;
    private long newUsers;
}
