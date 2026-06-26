package com.mitocode.customers.services.implementations;

import com.mitocode.customers.controllers.dtos.CustomerResource;
import com.mitocode.customers.persistence.entities.CustomerEntity;
import com.mitocode.customers.persistence.repositories.CustomerRepository;
import com.mitocode.customers.services.interfaces.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;

    @Override
    public List<CustomerResource> findAll() {
        return customerRepository.findAll().stream()
                .sorted(Comparator.comparing(CustomerEntity::getId))
                .map(this::toResource)
                .toList();
    }

    @Override
    public CustomerResource findById(Integer id) {
        return customerRepository.findById(id)
                .map(this::toResource)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
    }

    @Override
    public CustomerResource save(CustomerResource customer) {
        return toResource(customerRepository.save(toEntity(customer)));
    }

    @Override
    public void delete(Integer id) {
        customerRepository.deleteById(id);
    }

    private CustomerResource toResource(CustomerEntity entity) {
        var resource = new CustomerResource();
        resource.setId(entity.getId());
        resource.setDocType(entity.getDocType());
        resource.setDocNumber(entity.getDocNumber());
        resource.setNames(entity.getNames());
        resource.setSurnames(entity.getSurnames());
        resource.setEmail(entity.getEmail());
        resource.setPhone(entity.getPhone());
        resource.setAddress(entity.getAddress());
        resource.setMonthlyIncome(entity.getMonthlyIncome());
        resource.setOccupation(entity.getOccupation());
        resource.setStatus(entity.getStatus());
        return resource;
    }

    private CustomerEntity toEntity(CustomerResource resource) {
        var entity = resource.getId() != null
                ? customerRepository.findById(resource.getId()).orElse(new CustomerEntity())
                : new CustomerEntity();
        entity.setId(resource.getId());
        entity.setDocType(resource.getDocType());
        entity.setDocNumber(resource.getDocNumber());
        entity.setNames(resource.getNames());
        entity.setSurnames(resource.getSurnames());
        entity.setEmail(resource.getEmail());
        entity.setPhone(resource.getPhone());
        entity.setAddress(resource.getAddress());
        entity.setMonthlyIncome(resource.getMonthlyIncome());
        entity.setOccupation(resource.getOccupation());
        entity.setStatus(resource.getStatus() == null ? "Activo" : resource.getStatus());
        return entity;
    }
}
