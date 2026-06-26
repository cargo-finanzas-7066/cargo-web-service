package com.mitocode.financialinstitutions.services.interfaces;

import com.mitocode.financialinstitutions.controllers.dtos.FinancialInstitutionResource;

import java.util.List;

public interface FinancialInstitutionService {
    List<FinancialInstitutionResource> findAll();
    FinancialInstitutionResource findById(Integer id);
}
