package com.commutecarpool.repository;

import com.commutecarpool.entity.DriverVerification;
import com.commutecarpool.entity.VerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DriverVerificationRepository extends JpaRepository<DriverVerification, Long> {

    Optional<DriverVerification> findByDriverId(Long driverId);

    Optional<DriverVerification> findByApplicationNo(String applicationNo);

    @Query("SELECT v FROM DriverVerification v WHERE v.status = :status")
    Page<DriverVerification> findByStatus(@Param("status") VerificationStatus status, Pageable pageable);

    @Query("SELECT v FROM DriverVerification v WHERE v.driverId = :driverId")
    Page<DriverVerification> findByDriverId(@Param("driverId") Long driverId, Pageable pageable);

    boolean existsByDriverIdAndStatus(Long driverId, VerificationStatus status);

    @Query("SELECT EXISTS (SELECT 1 FROM DriverVerification v WHERE v.driverId = :driverId AND v.status = 'APPROVED' AND v.id = (SELECT MAX(v2.id) FROM DriverVerification v2 WHERE v2.driverId = :driverId))")
    boolean isDriverVerified(@Param("driverId") Long driverId);
}
