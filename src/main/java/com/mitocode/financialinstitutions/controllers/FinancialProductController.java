package com.mitocode.financialinstitutions.controllers;

import com.mitocode.financialinstitutions.controllers.dtos.FinancialProductResource;
import com.mitocode.financialinstitutions.controllers.dtos.FinancialProductSortField;
import com.mitocode.financialinstitutions.services.FinancialProductService;
import com.mitocode.shared.paging.PageableFactory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/financial-products") @RequiredArgsConstructor
public class FinancialProductController {
    private final FinancialProductService service;
    @GetMapping public Page<FinancialProductResource> findAll(@RequestParam(defaultValue="") String institution,
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size,
            @RequestParam(required=false) FinancialProductSortField sortBy, @RequestParam(defaultValue="ASC") Sort.Direction direction) {
        return service.findAll(institution, PageableFactory.of(page, size, sortBy, direction));
    }
    @GetMapping("/{id}") public FinancialProductResource findById(@PathVariable Integer id) { return service.findById(id); }
    @PostMapping @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FinancialProductResource> create(@Valid @RequestBody FinancialProductResource body) { return ResponseEntity.status(HttpStatus.CREATED).body(service.create(body)); }
    @PutMapping("/{id}") @PreAuthorize("hasRole('ADMIN')")
    public FinancialProductResource version(@PathVariable Integer id, @Valid @RequestBody FinancialProductResource body) { return service.createVersion(id, body); }
    @DeleteMapping("/{id}") @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) { service.deactivate(id); return ResponseEntity.noContent().build(); }
}
