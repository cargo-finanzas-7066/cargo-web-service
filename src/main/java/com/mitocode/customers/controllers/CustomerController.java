package com.mitocode.customers.controllers;

import com.mitocode.customers.controllers.dtos.CustomerResource;
import com.mitocode.customers.controllers.dtos.CustomerSortField;
import com.mitocode.customers.services.interfaces.CustomerService;
import com.mitocode.shared.paging.PageableFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/clients")
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService customerService;

    @GetMapping
    public Page<CustomerResource> getAll(@RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size,
            @RequestParam(required=false) CustomerSortField sortBy, @RequestParam(defaultValue="ASC") Sort.Direction direction) {
        return customerService.findAll(PageableFactory.of(page, size, sortBy, direction));
    }

    @GetMapping("/{id}")
    public CustomerResource getById(@PathVariable Integer id) {
        return customerService.findById(id);
    }

    @PostMapping
    public ResponseEntity<CustomerResource> create(@Valid @RequestBody CustomerResource customer) {
        customer.setId(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.save(customer));
    }

    @PutMapping("/{id}")
    public CustomerResource update(@PathVariable Integer id, @Valid @RequestBody CustomerResource customer) {
        customer.setId(id);
        return customerService.save(customer);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        customerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
