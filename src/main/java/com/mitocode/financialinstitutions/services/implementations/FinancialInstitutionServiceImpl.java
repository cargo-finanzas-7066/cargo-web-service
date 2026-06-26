package com.mitocode.financialinstitutions.services.implementations;

import com.mitocode.financialinstitutions.controllers.dtos.FinancialInstitutionResource;
import com.mitocode.financialinstitutions.persistence.entities.FinancialInstitutionEntity;
import com.mitocode.financialinstitutions.persistence.repositories.FinancialInstitutionRepository;
import com.mitocode.financialinstitutions.services.interfaces.FinancialInstitutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FinancialInstitutionServiceImpl implements FinancialInstitutionService {
    private final FinancialInstitutionRepository financialInstitutionRepository;

    @Override
    public List<FinancialInstitutionResource> findAll() {
        return financialInstitutionRepository.findByStatusAndCodeIsNotNullOrderByDisplayOrderAsc("Activo").stream()
                .map(this::toResource)
                .toList();
    }

    @Override
    public FinancialInstitutionResource findById(Integer id) {
        return financialInstitutionRepository.findById(id)
                .map(this::toResource)
                .orElseThrow(() -> new IllegalArgumentException("Entidad financiera no encontrada"));
    }

    private FinancialInstitutionResource toResource(FinancialInstitutionEntity entity) {
        var resource = new FinancialInstitutionResource();
        resource.setId(entity.getId());
        resource.setCode(entity.getCode());
        resource.setDisplayOrder(entity.getDisplayOrder());
        resource.setName(entity.getName());
        resource.setShortName(entity.getShortName());
        resource.setType(entity.getType());
        resource.setLogoText(entity.getLogoText());
        resource.setCurrency(entity.getCurrency());
        resource.setCreditType(entity.getCreditType());
        resource.setProduct(entity.getProduct());
        resource.setTeaPublishedLabel(entity.getTeaPublishedLabel());
        resource.setMinimumInitialLabel(entity.getMinimumInitialLabel());
        resource.setMaximumFinancingLabel(entity.getMaximumFinancingLabel());
        resource.setTermLabel(entity.getTermLabel());
        resource.setGraceLabel(entity.getGraceLabel());
        resource.setInsuranceSummaryLabel(entity.getInsuranceSummaryLabel());
        resource.setChargesSummaryLabel(entity.getChargesSummaryLabel());
        resource.setTea(entity.getTea());
        resource.setMinTerm(entity.getMinTerm());
        resource.setMaxTerm(entity.getMaxTerm());
        resource.setMinDownPayment(entity.getMinDownPayment());
        resource.setMaxFinancing(entity.getMaxFinancing());
        resource.setInsuranceDisbursement(entity.getInsuranceDisbursement());
        resource.setInsuranceVehicle(entity.getInsuranceVehicle());
        resource.setMonthlyFee(entity.getMonthlyFee());
        resource.setAdminCost(entity.getAdminCost());
        resource.setRatesJson(entity.getRatesJson());
        resource.setInsurancesJson(entity.getInsurancesJson());
        resource.setChargesJson(entity.getChargesJson());
        resource.setSourceName(entity.getSourceName());
        resource.setSourceDate(entity.getSourceDate());
        resource.setVerificationStatus(entity.getVerificationStatus());
        resource.setCanUseInSimulation(entity.getCanUseInSimulation());
        resource.setStatus(entity.getStatus());
        return resource;
    }
}
