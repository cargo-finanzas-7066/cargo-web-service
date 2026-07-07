package com.mitocode.financialinstitutions.persistence.repositories;

import com.mitocode.financialinstitutions.persistence.entities.FinancialProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FinancialProductRepository extends JpaRepository<FinancialProductEntity, Integer> {
    Page<FinancialProductEntity> findByActiveTrueAndFinancialInstitution_NameContainingIgnoreCase(String institution, Pageable pageable);
    Page<FinancialProductEntity> findByActiveTrueAndFinancialInstitution_CodeInAndFinancialInstitution_NameContainingIgnoreCase(Collection<String> codes, String institution, Pageable pageable);
    Optional<FinancialProductEntity> findByIdAndActiveTrue(Integer id);
    Optional<FinancialProductEntity> findFirstByFinancialInstitutionIdAndActiveTrueOrderByVersionDesc(Integer institutionId);
    List<FinancialProductEntity> findByIdInAndActiveTrue(Collection<Integer> ids);
    boolean existsByFinancialInstitutionId(Integer institutionId);
}
