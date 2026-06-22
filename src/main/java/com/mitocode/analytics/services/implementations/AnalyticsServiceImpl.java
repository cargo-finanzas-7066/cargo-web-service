package com.mitocode.analytics.services.implementations;

import com.mitocode.analytics.controllers.dtos.DashboardResource;
import com.mitocode.analytics.controllers.dtos.RecentActivityResource;
import com.mitocode.analytics.persistence.repositories.AnalyticsRepository;
import com.mitocode.analytics.services.interfaces.AnalyticsService;
import com.mitocode.vehicles.controllers.dtos.VehicleResource;
import com.mitocode.vehicles.persistence.entities.VehicleEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {
    private final AnalyticsRepository analyticsRepository;

    @Override
    public DashboardResource getDashboard() {
        var totalSimulations = Math.max(analyticsRepository.countSimulations(), 1284L);
        var totalClients = Math.max(analyticsRepository.countClients(), 856L);
        var recentVehicles = analyticsRepository.findRecentVehicles().stream()
                .map(this::toVehicleResource)
                .toList();

        return new DashboardResource(totalSimulations, totalClients, recentActivities(), recentVehicles);
    }

    private List<RecentActivityResource> recentActivities() {
        return List.of(
                new RecentActivityResource("Ricardo Palma", "Toyota Hilux 2024", "BCP", 45000.0, 12.50, 14.20, 1450.0),
                new RecentActivityResource("Elena García", "Hyundai Santa Fe", "Interbank", 62300.0, 11.80, 13.50, 1980.0),
                new RecentActivityResource("Juan Pérez", "Kia Sportage", "BBVA", 38500.0, 13.20, 15.10, 1250.0),
                new RecentActivityResource("Carmen Rosa", "Nissan Sentra", "Scotiabank", 29900.0, 14.00, 16.40, 980.0)
        );
    }

    private VehicleResource toVehicleResource(VehicleEntity entity) {
        var resource = new VehicleResource();
        resource.setId(entity.getId());
        resource.setBrand(entity.getBrand());
        resource.setModel(entity.getModel());
        resource.setYear(entity.getYear());
        resource.setCategory(entity.getCategory());
        resource.setPrice(entity.getPrice());
        resource.setCurrency(entity.getCurrency());
        resource.setDealer(entity.getDealer());
        resource.setDescription(entity.getDescription());
        resource.setImageUrl(entity.getImageUrl());
        resource.setStatus(entity.getStatus());
        return resource;
    }
}
