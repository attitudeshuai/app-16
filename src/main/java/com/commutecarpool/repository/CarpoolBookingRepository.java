package com.commutecarpool.repository;

import com.commutecarpool.entity.BookingStatus;
import com.commutecarpool.entity.CarpoolBooking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
