package com.commutecarpool.repository;

import com.commutecarpool.entity.DriverRestrictionLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DriverRestrictionLogRepository extends JpaRepository<DriverRestrictionLog, Long> {

    List<DriverRestrictionLog> findByRestrictionIdOrderByCreatedAtDesc(Long restrictionId);
}
