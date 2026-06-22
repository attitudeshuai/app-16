package com.commutecarpool.repository;

import com.commutecarpool.entity.DriverRestriction;
import com.commutecarpool.entity.RestrictionStatus;
import com.commutecarpool.entity.RestrictionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DriverRestrictionRepository extends JpaRepository<DriverRestriction, Long> {

    List<DriverRestriction> findByDriverIdAndStatus(Long driverId, RestrictionStatus status);

    Optional<DriverRestriction> findFirstByDriverIdAndStatusOrderByStartTimeDesc(Long driverId, RestrictionStatus status);

    @Query("SELECT COUNT(r) > 0 FROM DriverRestriction r WHERE r.driverId = :driverId AND r.status = :status AND r.endTime > :now")
    boolean existsActiveRestriction(@Param("driverId") Long driverId, @Param("status") RestrictionStatus status, @Param("now") LocalDateTime now);

    @Query("SELECT r FROM DriverRestriction r WHERE r.driverId = :driverId AND r.status = 'ACTIVE' AND r.endTime > :now")
    List<DriverRestriction> findActiveRestrictions(@Param("driverId") Long driverId, @Param("now") LocalDateTime now);

    Page<DriverRestriction> findByDriverId(Long driverId, Pageable pageable);

    Page<DriverRestriction> findByStatus(RestrictionStatus status, Pageable pageable);

    Page<DriverRestriction> findByRestrictionType(RestrictionType type, Pageable pageable);

    @Query("SELECT COUNT(r) FROM DriverRestriction r WHERE r.driverId = :driverId AND r.restrictionType = :type AND r.createdAt >= :since")
    long countByDriverIdAndTypeSince(@Param("driverId") Long driverId, @Param("type") RestrictionType type, @Param("since") LocalDateTime since);

    Page<DriverRestriction> findByStatusAndRestrictionType(RestrictionStatus status, RestrictionType type, Pageable pageable);

    @Query("SELECT r FROM DriverRestriction r WHERE r.status = :status AND r.endTime <= :now")
    List<DriverRestriction> findExpiredActiveRestrictions(@Param("status") RestrictionStatus status, @Param("now") LocalDateTime now);
}
