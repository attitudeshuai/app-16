package com.commutecarpool.repository;

import com.commutecarpool.entity.Route;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RouteRepository extends JpaRepository<Route, Long> {

    Page<Route> findByOwnerId(Long ownerId, Pageable pageable);

    Page<Route> findByIsActiveTrue(Pageable pageable);

    Page<Route> findByStartLocationContainingOrEndLocationContaining(String start, String end, Pageable pageable);

    Page<Route> findByIsActiveTrueAndStartLocationContainingOrEndLocationContaining(String start, String end, Pageable pageable);
}
