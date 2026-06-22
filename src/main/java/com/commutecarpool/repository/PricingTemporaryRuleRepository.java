package com.commutecarpool.repository;

import com.commutecarpool.entity.PricingTemporaryRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface PricingTemporaryRuleRepository extends JpaRepository<PricingTemporaryRule, Long> {

    List<PricingTemporaryRule> findByIsActiveTrueOrderByPriorityDesc();

    @Query("SELECT r FROM PricingTemporaryRule r WHERE r.isActive = true " +
            "AND r.startDate <= :date AND r.endDate >= :date " +
            "AND (r.startTime IS NULL OR r.startTime <= :time) " +
            "AND (r.endTime IS NULL OR r.endTime >= :time) " +
            "ORDER BY r.priority DESC")
    List<PricingTemporaryRule> findActiveRulesForDateTime(@Param("date") LocalDate date, @Param("time") LocalTime time);

    List<PricingTemporaryRule> findAllByOrderByPriorityDescCreatedAtDesc();
}
