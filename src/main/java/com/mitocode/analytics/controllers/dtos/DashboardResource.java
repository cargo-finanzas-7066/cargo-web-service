package com.mitocode.analytics.controllers.dtos;

import com.mitocode.vehicles.controllers.dtos.VehicleResource;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DashboardResource {
    private Long totalSimulations;
    private Long totalClients;
    private List<RecentActivityResource> recentActivities;
    private List<VehicleResource> recentVehicles;
}
