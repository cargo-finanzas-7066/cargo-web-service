package com.mitocode.customers.controllers;

import com.mitocode.customers.controllers.dtos.CustomerResource;
import com.mitocode.customers.services.interfaces.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService customerService;

    @GetMapping
    public List<CustomerResource> getAll() {
        return customerService.findAll();
    }

    @GetMapping("/{id}")
    public CustomerResource getById(@PathVariable Integer id) {
        return customerService.findById(id);
    }

    @PostMapping
    public ResponseEntity<CustomerResource> create(@RequestBody CustomerResource customer) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.save(customer));
    }

    @PutMapping("/{id}")
    public CustomerResource update(@PathVariable Integer id, @RequestBody CustomerResource customer) {
        customer.setId(id);
        return customerService.save(customer);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        customerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
