package com.commutecarpool.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatsOverview {

    private long totalUsers;
    private long totalRoutes;
    private long totalCarpools;
    private long totalBookings;
    private long totalRatings;
    private long activeCarpools;
    private long completedCarpools;
}
