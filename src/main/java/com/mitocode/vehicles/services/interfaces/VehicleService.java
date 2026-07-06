package com.mitocode.vehicles.services.interfaces;

import com.mitocode.vehicles.controllers.dtos.VehicleResource;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VehicleService {
    Page<VehicleResource> findAll(String brand, Pageable pageable);
    VehicleResource findById(Integer id);
    VehicleResource save(VehicleResource vehicle);
    void delete(Integer id);
}
