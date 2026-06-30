package com.mitocode.financialinstitutions.services.implementations;

import com.mitocode.financialinstitutions.controllers.dtos.FinancialInstitutionResource;
import com.mitocode.financialinstitutions.persistence.entities.FinancialInstitutionEntity;
import com.mitocode.financialinstitutions.persistence.repositories.FinancialInstitutionRepository;
import com.mitocode.financialinstitutions.services.interfaces.FinancialInstitutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
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
        resource.setName(clean(entity.getName()));
        resource.setShortName(clean(entity.getShortName()));
        resource.setType(clean(entity.getType()));
        resource.setLogoText(clean(entity.getLogoText()));
        resource.setCurrency(clean(entity.getCurrency()));
        resource.setCreditType(clean(entity.getCreditType()));
        resource.setProduct(clean(entity.getProduct()));
        resource.setTeaPublishedLabel(clean(entity.getTeaPublishedLabel()));
        resource.setMinimumInitialLabel(clean(entity.getMinimumInitialLabel()));
        resource.setMaximumFinancingLabel(clean(entity.getMaximumFinancingLabel()));
        resource.setTermLabel(clean(entity.getTermLabel()));
        resource.setGraceLabel(clean(entity.getGraceLabel()));
        resource.setInsuranceSummaryLabel(clean(entity.getInsuranceSummaryLabel()));
        resource.setChargesSummaryLabel(clean(entity.getChargesSummaryLabel()));
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
        resource.setSourceName(clean(entity.getSourceName()));
        resource.setSourceDate(entity.getSourceDate());
        resource.setVerificationStatus(entity.getVerificationStatus());
        resource.setCanUseInSimulation(entity.getCanUseInSimulation());
        resource.setStatus(entity.getStatus());
        return resource;
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }
        String cleaned = value;
        if (cleaned.contains("Ã") || cleaned.contains("Â") || cleaned.contains("ï¿½")) {
            cleaned = new String(cleaned.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        }
        return cleaned
                .replace("Ã¡", "\u00e1")
                .replace("Ã©", "\u00e9")
                .replace("Ã­", "\u00ed")
                .replace("Ã³", "\u00f3")
                .replace("Ãº", "\u00fa")
                .replace("Ã±", "\u00f1")
                .replace("Â¿", "\u00bf")
                .replace("Â¡", "\u00a1");
    }
}
