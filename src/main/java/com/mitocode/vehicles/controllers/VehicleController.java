package com.mitocode.vehicles.controllers;

import com.mitocode.vehicles.controllers.dtos.VehicleResource;
import com.mitocode.vehicles.controllers.dtos.VehicleSortField;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.access.prepost.PreAuthorize;
import com.mitocode.shared.paging.PageableFactory;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/vehicles")
@RequiredArgsConstructor
public class VehicleController {
    private final VehicleService vehicleService;

    @GetMapping
    public Page<VehicleResource> getAll(@RequestParam(defaultValue="") String brand,
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size,
            @RequestParam(required=false) VehicleSortField sortBy, @RequestParam(defaultValue="ASC") Sort.Direction direction) {
        return vehicleService.findAll(brand, PageableFactory.of(page, size, sortBy, direction));
    }

    @GetMapping("/catalog")
    public Page<VehicleResource> getCatalog(@RequestParam(defaultValue="") String brand,
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size,
            @RequestParam(required=false) VehicleSortField sortBy, @RequestParam(defaultValue="ASC") Sort.Direction direction) {
        return vehicleService.findAll(brand, PageableFactory.of(page, size, sortBy, direction));
    }

    @GetMapping("/{id}")
    public VehicleResource getById(@PathVariable Integer id) {
        return vehicleService.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VehicleResource> create(@Valid @RequestBody VehicleResource vehicle) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vehicleService.save(vehicle));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public VehicleResource update(@PathVariable Integer id, @Valid @RequestBody VehicleResource vehicle) {
        vehicle.setId(id);
        return vehicleService.save(vehicle);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        vehicleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
