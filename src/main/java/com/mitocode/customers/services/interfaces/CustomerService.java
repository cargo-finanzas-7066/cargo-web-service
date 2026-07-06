package com.mitocode.customers.services.interfaces;

import com.mitocode.customers.controllers.dtos.CustomerResource;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerService {
    Page<CustomerResource> findAll(Pageable pageable);
    CustomerResource findById(Integer id);
    CustomerResource save(CustomerResource customer);
    void delete(Integer id);
}
