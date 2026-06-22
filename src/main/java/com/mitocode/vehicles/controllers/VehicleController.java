package com.mitocode.vehicles.controllers;

import com.mitocode.vehicles.controllers.dtos.VehicleResource;
import com.mitocode.vehicles.services.interfaces.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {
    private final VehicleService vehicleService;

    @GetMapping
    public List<VehicleResource> getAll() {
        return vehicleService.findAll();
    }

    @GetMapping("/catalog")
    public List<VehicleResource> getCatalog() {
        return vehicleService.findAll();
    }

    @GetMapping("/{id}")
    public VehicleResource getById(@PathVariable Integer id) {
        return vehicleService.findById(id);
    }

    @PostMapping
    public ResponseEntity<VehicleResource> create(@RequestBody VehicleResource vehicle) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vehicleService.save(vehicle));
    }

    @PutMapping("/{id}")
    public VehicleResource update(@PathVariable Integer id, @RequestBody VehicleResource vehicle) {
        vehicle.setId(id);
        return vehicleService.save(vehicle);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        vehicleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
