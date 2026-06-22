package com.commutecarpool.repository;

import com.commutecarpool.entity.BookingStatus;
import com.commutecarpool.entity.CarpoolBooking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CarpoolBookingRepository extends JpaRepository<CarpoolBooking, Long> {

    Page<CarpoolBooking> findByCarpoolId(Long carpoolId, Pageable pageable);

    Page<CarpoolBooking> findByPassengerId(Long passengerId, Pageable pageable);

    Page<CarpoolBooking> findByStatus(BookingStatus status, Pageable pageable);

    List<CarpoolBooking> findByCarpoolIdAndStatus(Long carpoolId, BookingStatus status);

    long countByStatus(BookingStatus status);

    Page<CarpoolBooking> findByCarpoolIdAndPassengerId(Long carpoolId, Long passengerId, Pageable pageable);

    Page<CarpoolBooking> findByCarpoolIdAndStatus(Long carpoolId, BookingStatus status, Pageable pageable);

    Page<CarpoolBooking> findByPassengerIdAndStatus(Long passengerId, BookingStatus status, Pageable pageable);

    Page<CarpoolBooking> findByCarpoolIdAndPassengerIdAndStatus(Long carpoolId, Long passengerId, BookingStatus status, Pageable pageable);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT cb FROM CarpoolBooking cb JOIN Carpool c ON cb.carpoolId = c.id " +
            "WHERE cb.id = :bookingId AND c.driverId = :driverId")
    CarpoolBooking findByIdAndDriverId(@Param("bookingId") Long bookingId, @Param("driverId") Long driverId);

    @Query("SELECT cb FROM CarpoolBooking cb JOIN Carpool c ON cb.carpoolId = c.id " +
            "WHERE cb.status IN ('PENDING', 'CONFIRMED') " +
            "AND cb.reminderSmsSent = false " +
            "AND c.tripDate IS NOT NULL")
    List<CarpoolBooking> findBookingsPendingReminder();
}
