package com.mitocode.financialinstitutions.persistence.repositories;

import com.mitocode.financialinstitutions.persistence.entities.FinancialInstitutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FinancialInstitutionRepository extends JpaRepository<FinancialInstitutionEntity, Integer> {
    List<FinancialInstitutionEntity> findByStatusAndCodeIsNotNullOrderByDisplayOrderAsc(String status);
    Optional<FinancialInstitutionEntity> findByCode(String code);
}
