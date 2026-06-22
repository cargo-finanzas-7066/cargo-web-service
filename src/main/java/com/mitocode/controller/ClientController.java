package com.mitocode.controller;

import com.mitocode.entity.ClientEntity;
import com.mitocode.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {
    private final ClientService service;

    @GetMapping
    public List<ClientEntity> getAll() { return service.findAll(); }

    @GetMapping("/{id}")
    public ClientEntity getById(@PathVariable Integer id) { return service.findById(id); }

    @PostMapping
    public ResponseEntity<ClientEntity> create(@RequestBody ClientEntity c) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(c));
    }

    @PutMapping("/{id}")
    public ClientEntity update(@PathVariable Integer id, @RequestBody ClientEntity c) {
        c.setId(id);
        return service.save(c);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
