package com.mitocode.analytics.controllers;

import com.mitocode.analytics.controllers.dtos.DashboardResource;
import com.mitocode.analytics.services.interfaces.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    public DashboardResource getDashboard() {
        return analyticsService.getDashboard();
    }
}
