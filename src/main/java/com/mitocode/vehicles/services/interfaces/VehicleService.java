package com.mitocode.vehicles.services.interfaces;

import com.mitocode.vehicles.controllers.dtos.VehicleResource;

import java.util.List;

public interface VehicleService {
    List<VehicleResource> findAll();
    VehicleResource findById(Integer id);
    VehicleResource save(VehicleResource vehicle);
    void delete(Integer id);
}
