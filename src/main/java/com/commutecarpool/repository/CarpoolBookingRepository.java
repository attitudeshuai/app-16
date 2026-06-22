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

    long countByPassengerId(Long passengerId);

    @Query("SELECT cb FROM CarpoolBooking cb JOIN Carpool c ON cb.carpoolId = c.id " +
            "WHERE cb.id = :bookingId AND c.driverId = :driverId")
    CarpoolBooking findByIdAndDriverId(@Param("bookingId") Long bookingId, @Param("driverId") Long driverId);

    @Query(value = "SELECT cb.* FROM carpool_bookings cb " +
            "INNER JOIN carpools c ON cb.carpool_id = c.id " +
            "INNER JOIN routes r ON c.route_id = r.id " +
            "WHERE cb.status IN ('PENDING', 'CONFIRMED') " +
            "AND cb.reminder_sms_sent = false " +
            "AND cb.emergency_contact_phone IS NOT NULL " +
            "AND cb.emergency_contact_name IS NOT NULL " +
            "AND c.trip_date IS NOT NULL " +
            "AND r.start_time IS NOT NULL " +
            "AND STR_TO_DATE(CONCAT(c.trip_date, ' ', r.start_time), '%Y-%m-%d %H:%i') " +
            "    BETWEEN :windowStart AND :windowEnd",
            nativeQuery = true)
    List<CarpoolBooking> findBookingsForReminder(
            @Param("windowStart") LocalDateTime windowStart,
            @Param("windowEnd") LocalDateTime windowEnd);
}
