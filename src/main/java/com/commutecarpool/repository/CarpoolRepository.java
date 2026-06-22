package com.commutecarpool.repository;

import com.commutecarpool.entity.Carpool;
import com.commutecarpool.entity.CarpoolStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface CarpoolRepository extends JpaRepository<Carpool, Long> {

    Page<Carpool> findByDriverId(Long driverId, Pageable pageable);

    Page<Carpool> findByRouteId(Long routeId, Pageable pageable);

    Page<Carpool> findByStatus(CarpoolStatus status, Pageable pageable);

    Page<Carpool> findByTripDateBetween(LocalDate start, LocalDate end, Pageable pageable);

    List<Carpool> findByStatusAndTripDateBefore(CarpoolStatus status, LocalDate date);

    long countByStatus(CarpoolStatus status);

    Page<Carpool> findByStatusAndTripDateBetween(CarpoolStatus status, LocalDate start, LocalDate end, Pageable pageable);

    long countByTripDate(LocalDate tripDate);

    @Query("SELECT COUNT(c) FROM Carpool c WHERE c.routeId = :routeId " +
            "AND c.tripDate BETWEEN :startDate AND :endDate " +
            "AND c.status IN (com.commutecarpool.entity.CarpoolStatus.OPEN, com.commutecarpool.entity.CarpoolStatus.FULL)")
    long countActiveCarpoolsByRouteAndDateRange(@Param("routeId") Long routeId,
                                                @Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(b) FROM Carpool c JOIN CarpoolBooking b ON c.id = b.carpoolId " +
            "WHERE c.routeId = :routeId AND c.tripDate BETWEEN :startDate AND :endDate " +
            "AND b.status != com.commutecarpool.entity.BookingStatus.CANCELLED")
    long countBookingsByRouteAndDateRange(@Param("routeId") Long routeId,
                                          @Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(c) FROM Carpool c WHERE c.driverId = :driverId AND c.status = 'CANCELLED' AND c.createdAt >= :since")
    long countNoShowByDriverIdSince(@Param("driverId") Long driverId, @Param("since") LocalDateTime since);
}
