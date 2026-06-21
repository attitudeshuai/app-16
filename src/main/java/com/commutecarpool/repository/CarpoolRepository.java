package com.commutecarpool.repository;

import com.commutecarpool.entity.Carpool;
import com.commutecarpool.entity.CarpoolStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
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
}
