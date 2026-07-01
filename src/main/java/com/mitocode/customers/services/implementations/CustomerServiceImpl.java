package com.mitocode.customers.services.implementations;

import com.mitocode.customers.controllers.dtos.CustomerResource;
import com.mitocode.customers.persistence.entities.CustomerEntity;
import com.mitocode.customers.persistence.repositories.CustomerRepository;
import com.mitocode.customers.services.interfaces.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.mitocode.iam.services.implementations.CurrentUserService;
import com.mitocode.iam.persistence.entities.Role;
import com.mitocode.exception.ResourceNotFoundException;
import com.mitocode.exception.ConflictException;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;
    private final CurrentUserService currentUserService;

    @Override
    public Page<CustomerResource> findAll(Pageable pageable) {
        var user = currentUserService.requireUser();
        var page = user.getRole() == Role.ADMIN
                ? customerRepository.findByArchivedFalse(pageable)
                : customerRepository.findByOwnerIdAndArchivedFalse(user.getId(), pageable);
        return page.map(this::toResource);
    }

    @Override
    public CustomerResource findById(Integer id) {
        var user = currentUserService.requireUser();
        var found = user.getRole() == Role.ADMIN
                ? customerRepository.findByIdAndArchivedFalse(id)
                : customerRepository.findByIdAndOwnerIdAndArchivedFalse(id, user.getId());
        return found
                .map(this::toResource)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
    }

    @Override
    public CustomerResource save(CustomerResource customer) {
        var user = currentUserService.requireUser();
        if (isNew(customer.getId()) && customerRepository.existsByOwnerIdAndDocTypeIgnoreCaseAndDocNumber(
                user.getId(), customer.getDocType(), customer.getDocNumber())) {
            throw new ConflictException("Ya existe un cliente con ese documento");
        }
        return toResource(customerRepository.save(toEntity(customer, user)));
    }

    private boolean isNew(Integer id) {
        return id == null || id == 0;
    }

    @Override
    public void delete(Integer id) {
        var user = currentUserService.requireUser();
        var entity = user.getRole() == Role.ADMIN ? customerRepository.findByIdAndArchivedFalse(id)
                : customerRepository.findByIdAndOwnerIdAndArchivedFalse(id, user.getId());
        var customer = entity.orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
        customer.setArchived(true);
        customer.setStatus("Archivado");
        customerRepository.save(customer);
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

    private CustomerEntity toEntity(CustomerResource resource, com.mitocode.iam.persistence.entities.UserEntity user) {
        var entity = isNew(resource.getId()) ? new CustomerEntity() : (user.getRole() == Role.ADMIN
                ? customerRepository.findByIdAndArchivedFalse(resource.getId())
                : customerRepository.findByIdAndOwnerIdAndArchivedFalse(resource.getId(), user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
        if (entity.getId() == null) entity.setOwner(user);
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
        entity.setArchived(false);
        return entity;
    }
}
