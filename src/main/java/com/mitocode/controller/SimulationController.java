package com.mitocode.controller;

import com.mitocode.dto.SimulationRequest;
import com.mitocode.dto.SimulationResult;
import com.mitocode.entity.SimulationEntity;
import com.mitocode.service.SimulationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/simulations")
@RequiredArgsConstructor
public class SimulationController {
    private final SimulationService service;

    @GetMapping
    public List<SimulationEntity> getAll() { return service.findAll(); }

    @GetMapping("/{id}")
    public SimulationEntity getById(@PathVariable Integer id) { return service.findById(id); }

    @PostMapping
    public ResponseEntity<SimulationEntity> create(@RequestBody SimulationRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(req));
    }

    @PutMapping("/{id}")
    public SimulationEntity update(@PathVariable Integer id, @RequestBody SimulationRequest req) {
        req.setId(id);
        return service.save(req);
    }

    @PostMapping("/{id}/calculate")
    public SimulationResult calculate(@PathVariable Integer id) {
        return service.calculate(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
