package com.commutecarpool.repository;

import com.commutecarpool.entity.PricingConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PricingConfigRepository extends JpaRepository<PricingConfig, Long> {

    Optional<PricingConfig> findByConfigKey(String configKey);
}
