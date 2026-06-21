package com.commutecarpool.repository;

import com.commutecarpool.entity.DriverVerificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DriverVerificationLogRepository extends JpaRepository<DriverVerificationLog, Long> {

    @Query("SELECT l FROM DriverVerificationLog l WHERE l.verification.id = :verificationId ORDER BY l.createdAt DESC")
    List<DriverVerificationLog> findByVerificationId(@Param("verificationId") Long verificationId);
}
