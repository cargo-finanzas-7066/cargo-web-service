package com.mitocode.customers.services.interfaces;

import com.mitocode.customers.controllers.dtos.CustomerResource;

import java.util.List;

public interface CustomerService {
    List<CustomerResource> findAll();
    CustomerResource findById(Integer id);
    CustomerResource save(CustomerResource customer);
    void delete(Integer id);
}
