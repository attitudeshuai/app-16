package com.commutecarpool.repository;

import com.commutecarpool.entity.EmergencyContact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmergencyContactRepository extends JpaRepository<EmergencyContact, Long> {

    List<EmergencyContact> findByPassengerId(Long passengerId);

    Optional<EmergencyContact> findByIdAndPassengerId(Long id, Long passengerId);

    long countByPassengerId(Long passengerId);

    boolean existsByPassengerId(Long passengerId);
}
