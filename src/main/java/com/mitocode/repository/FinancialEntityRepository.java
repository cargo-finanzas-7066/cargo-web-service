package com.mitocode.repository;

import com.mitocode.entity.FinancialEntityEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FinancialEntityRepository extends JpaRepository<FinancialEntityEntity, Integer> {}
