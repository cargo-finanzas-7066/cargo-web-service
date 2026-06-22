package com.mitocode.vehicles.services.implementations;

import com.mitocode.vehicles.controllers.dtos.VehicleResource;
import com.mitocode.vehicles.persistence.entities.VehicleEntity;
import com.mitocode.vehicles.persistence.repositories.VehicleRepository;
import com.mitocode.vehicles.services.interfaces.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {
    private final VehicleRepository vehicleRepository;

    @Override
    public List<VehicleResource> findAll() {
        return vehicleRepository.findAll().stream()
                .sorted(Comparator.comparing(VehicleEntity::getBrand).thenComparing(VehicleEntity::getModel))
                .map(this::toResource)
                .toList();
    }

    @Override
    public VehicleResource findById(Integer id) {
        return vehicleRepository.findById(id)
                .map(this::toResource)
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado"));
    }

    @Override
    public VehicleResource save(VehicleResource vehicle) {
        return toResource(vehicleRepository.save(toEntity(vehicle)));
    }

    @Override
    public void delete(Integer id) {
        vehicleRepository.deleteById(id);
    }

    private VehicleResource toResource(VehicleEntity entity) {
        var resource = new VehicleResource();
        resource.setId(entity.getId());
        resource.setCode(entity.getCode());
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

    private VehicleEntity toEntity(VehicleResource resource) {
        var entity = resource.getId() != null
                ? vehicleRepository.findById(resource.getId()).orElse(new VehicleEntity())
                : new VehicleEntity();
        entity.setId(resource.getId());
        entity.setCode(resource.getCode());
        entity.setBrand(resource.getBrand());
        entity.setModel(resource.getModel());
        entity.setYear(resource.getYear());
        entity.setCategory(resource.getCategory());
        entity.setPrice(resource.getPrice());
        entity.setCurrency(resource.getCurrency() == null ? "PEN" : resource.getCurrency());
        entity.setDealer(resource.getDealer());
        entity.setDescription(resource.getDescription());
        entity.setImageUrl(resource.getImageUrl());
        entity.setStatus(resource.getStatus() == null ? "Disponible" : resource.getStatus());
        return entity;
    }
}
