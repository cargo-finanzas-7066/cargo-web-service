package com.mitocode.analytics.persistence.repositories;

import com.mitocode.repository.ClientRepository;
import com.mitocode.repository.SimulationRepository;
import com.mitocode.vehicles.persistence.entities.VehicleEntity;
import com.mitocode.vehicles.persistence.repositories.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AnalyticsRepository {
    private final SimulationRepository simulationRepository;
    private final ClientRepository clientRepository;
    private final VehicleRepository vehicleRepository;

    public long countSimulations() {
        return simulationRepository.count();
    }

    public long countClients() {
        return clientRepository.count();
    }

    public List<VehicleEntity> findRecentVehicles() {
        return vehicleRepository.findAll().stream().limit(3).toList();
    }
}
