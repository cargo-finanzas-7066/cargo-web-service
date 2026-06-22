package com.mitocode.repository;

import com.mitocode.entity.SimulationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SimulationRepository extends JpaRepository<SimulationEntity, Integer> {}
