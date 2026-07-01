package com.mitocode.customers.persistence.repositories;

import com.mitocode.customers.persistence.entities.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<CustomerEntity, Integer> {
    Page<CustomerEntity> findByOwnerIdAndArchivedFalse(Integer ownerId, Pageable pageable);
    Page<CustomerEntity> findByArchivedFalse(Pageable pageable);
    Optional<CustomerEntity> findByIdAndOwnerIdAndArchivedFalse(Integer id, Integer ownerId);
    Optional<CustomerEntity> findByIdAndArchivedFalse(Integer id);
    boolean existsByOwnerIdAndDocTypeIgnoreCaseAndDocNumber(Integer ownerId, String docType, String docNumber);
    long countByOwnerIdAndArchivedFalse(Integer ownerId);
    long countByArchivedFalse();
}
