package com.mitocode.controller;

import com.mitocode.entity.FinancialEntityEntity;
import com.mitocode.service.FinancialEntityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/entities")
@RequiredArgsConstructor
public class FinancialEntityController {
    private final FinancialEntityService service;

    @GetMapping
    public List<FinancialEntityEntity> getAll() { return service.findAll(); }

    @GetMapping("/{id}")
    public FinancialEntityEntity getById(@PathVariable Integer id) { return service.findById(id); }

    @PostMapping
    public ResponseEntity<FinancialEntityEntity> create(@RequestBody FinancialEntityEntity e) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(e));
    }

    @PutMapping("/{id}")
    public FinancialEntityEntity update(@PathVariable Integer id, @RequestBody FinancialEntityEntity e) {
        e.setId(id);
        return service.save(e);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
