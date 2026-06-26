package com.mitocode.customers.persistence.repositories;

import com.mitocode.customers.persistence.entities.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<CustomerEntity, Integer> {
}
