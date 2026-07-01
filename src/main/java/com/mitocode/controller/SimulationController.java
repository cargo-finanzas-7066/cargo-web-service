package com.mitocode.controller;

import com.mitocode.dto.*;
import com.mitocode.service.SimulationService;
import com.mitocode.shared.paging.PageableFactory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequiredArgsConstructor
public class SimulationController {
    private final SimulationService service;

    @PostMapping("/quotes")
    public List<QuoteResource> quote(@Valid @RequestBody QuoteRequest request) { return service.quote(request); }

    @PostMapping("/simulations")
    public ResponseEntity<SimulationResource> save(@Valid @RequestBody SimulationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(request));
    }
    @GetMapping("/simulations")
    public Page<SimulationResource> findAll(@RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size,
            @RequestParam(required=false) SimulationSortField sortBy, @RequestParam(defaultValue="ASC") Sort.Direction direction) {
        return service.findAll(PageableFactory.of(page, size, sortBy, direction));
    }
    @GetMapping("/simulations/{id}")
    public SimulationResource findById(@PathVariable Integer id) { return service.findById(id); }
    @DeleteMapping("/simulations/{id}")
    public ResponseEntity<Void> archive(@PathVariable Integer id) { service.archive(id); return ResponseEntity.noContent().build(); }
}
