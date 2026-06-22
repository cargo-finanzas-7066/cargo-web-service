package com.mitocode.vehicles.persistence.repositories;

import com.mitocode.vehicles.persistence.entities.VehicleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<VehicleEntity, Integer> {
}
