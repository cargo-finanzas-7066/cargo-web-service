package com.mitocode.vehicles.persistence.repositories;

import com.mitocode.vehicles.persistence.entities.VehicleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VehicleRepository extends JpaRepository<VehicleEntity, Integer> {
    Optional<VehicleEntity> findByCode(String code);
}
