package com.mitocode.vehicles.persistence.repositories;

import com.mitocode.vehicles.persistence.entities.VehicleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VehicleRepository extends JpaRepository<VehicleEntity, Integer> {
    Optional<VehicleEntity> findByCode(String code);
    Page<VehicleEntity> findByActiveTrueAndBrandContainingIgnoreCase(String brand, Pageable pageable);
    Optional<VehicleEntity> findByIdAndActiveTrue(Integer id);
    Page<VehicleEntity> findByActiveTrue(Pageable pageable);
}
