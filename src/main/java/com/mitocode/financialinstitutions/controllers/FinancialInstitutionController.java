package com.mitocode.financialinstitutions.controllers;

import com.mitocode.financialinstitutions.controllers.dtos.FinancialInstitutionResource;
import com.mitocode.financialinstitutions.services.interfaces.FinancialInstitutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/financial-institutions")
@RequiredArgsConstructor
public class FinancialInstitutionController {
    private final FinancialInstitutionService financialInstitutionService;

    @GetMapping
    public List<FinancialInstitutionResource> getAll() {
        return financialInstitutionService.findAll();
    }

    @GetMapping("/{id}")
    public FinancialInstitutionResource getById(@PathVariable Integer id) {
        return financialInstitutionService.findById(id);
    }
}
