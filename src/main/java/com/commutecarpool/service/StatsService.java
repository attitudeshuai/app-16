package com.commutecarpool.service;

import com.commutecarpool.dto.stats.StatsOverview;
import com.commutecarpool.dto.stats.StatsTrend;
import com.commutecarpool.entity.CarpoolStatus;
import com.commutecarpool.repository.CarpoolBookingRepository;
import com.commutecarpool.repository.CarpoolRatingRepository;
import com.commutecarpool.repository.CarpoolRepository;
import com.commutecarpool.repository.RouteRepository;
import com.commutecarpool.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final UserRepository userRepository;
    private final RouteRepository routeRepository;
    private final CarpoolRepository carpoolRepository;
    private final CarpoolBookingRepository carpoolBookingRepository;
    private final CarpoolRatingRepository carpoolRatingRepository;

    public StatsOverview getOverview() {
        long totalUsers = userRepository.count();
        long totalRoutes = routeRepository.count();
        long totalCarpools = carpoolRepository.count();
        long totalBookings = carpoolBookingRepository.count();
        long totalRatings = carpoolRatingRepository.count();
        long activeCarpools = carpoolRepository.countByStatus(CarpoolStatus.OPEN);
        long completedCarpools = carpoolRepository.countByStatus(CarpoolStatus.COMPLETED);
        return new StatsOverview(totalUsers, totalRoutes, totalCarpools, totalBookings, totalRatings, activeCarpools, completedCarpools);
    }

    public List<StatsTrend> getTrend(LocalDate startDate, LocalDate endDate) {
        return dateRange(startDate, endDate)
                .map(date -> {
                    long carpools = carpoolRepository.countByTripDate(date);
                    long bookings = carpoolBookingRepository.countByCreatedAtBetween(
                            date.atStartOfDay(),
                            date.atTime(LocalTime.MAX)
                    );
                    long newUsers = userRepository.countByCreatedAtBetween(
                            date.atStartOfDay(),
                            date.atTime(LocalTime.MAX)
                    );
                    return new StatsTrend(date.toString(), carpools, bookings, newUsers);
                })
                .collect(Collectors.toList());
    }

    private Stream<LocalDate> dateRange(LocalDate start, LocalDate end) {
        return Stream.iterate(start, date -> !date.isAfter(end), date -> date.plusDays(1));
    }
}
