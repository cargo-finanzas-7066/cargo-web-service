package com.mitocode.analytics.services.implementations;

import com.mitocode.analytics.controllers.dtos.*;
import com.mitocode.analytics.services.interfaces.AnalyticsService;
import com.mitocode.customers.persistence.repositories.CustomerRepository;
import com.mitocode.financialinstitutions.persistence.repositories.FinancialInstitutionRepository;
import com.mitocode.iam.persistence.entities.Role;
import com.mitocode.iam.services.implementations.CurrentUserService;
import com.mitocode.repository.SimulationRepository;
import com.mitocode.vehicles.controllers.dtos.VehicleResource;
import com.mitocode.vehicles.persistence.entities.VehicleEntity;
import com.mitocode.vehicles.persistence.repositories.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service @RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {
    private final SimulationRepository simulationRepository;
    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final FinancialInstitutionRepository institutionRepository;
    private final CurrentUserService currentUserService;

    @Override @Transactional(readOnly=true)
    public DashboardResource getDashboard() {
        var user=currentUserService.requireUser(); boolean admin=user.getRole()==Role.ADMIN;
        long simulations=admin?simulationRepository.countByArchivedFalse():simulationRepository.countByOwnerIdAndArchivedFalse(user.getId());
        long clients=admin?customerRepository.countByArchivedFalse():customerRepository.countByOwnerIdAndArchivedFalse(user.getId());
        var page=admin?simulationRepository.findByArchivedFalseOrderByCreatedAtTimestampDesc(PageRequest.of(0,4))
                :simulationRepository.findByOwnerIdAndArchivedFalseOrderByCreatedAtTimestampDesc(user.getId(),PageRequest.of(0,4));
        var activities=page.stream().map(s->{
            var c=customerRepository.findById(s.getClientId()).orElse(null);var v=vehicleRepository.findById(s.getVehicleId()).orElse(null);
            var i=s.getEntityId()==null?null:institutionRepository.findById(s.getEntityId()).orElse(null);
            return new RecentActivityResource(c==null?"Cliente":c.getNames()+" "+c.getSurnames(),v==null?"Vehículo":v.getBrand()+" "+v.getModel(),
                    i==null?"Entidad":i.getShortName(),s.getFinancedAmount(),s.getTea(),s.getTcea(),s.getMonthlyPayment());
        }).toList();
        var vehicles=vehicleRepository.findByActiveTrue(PageRequest.of(0,3)).stream().map(this::vehicle).toList();
        return new DashboardResource(simulations,clients,activities,vehicles);
    }
    private VehicleResource vehicle(VehicleEntity e){var r=new VehicleResource();r.setId(e.getId());r.setCode(e.getCode());r.setBrand(e.getBrand());r.setModel(e.getModel());
        r.setYear(e.getYear());r.setCategory(e.getCategory());r.setPrice(e.getPrice());r.setCurrency(e.getCurrency());r.setDealer(e.getDealer());r.setDescription(e.getDescription());
        r.setImageUrl(e.getImageUrl());r.setStatus(e.getStatus());return r;}
}
