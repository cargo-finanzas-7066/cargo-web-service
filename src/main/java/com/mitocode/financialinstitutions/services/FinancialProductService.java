package com.mitocode.financialinstitutions.services;

import com.mitocode.exception.ResourceNotFoundException;
import com.mitocode.financialinstitutions.controllers.dtos.FinancialProductResource;
import com.mitocode.financialinstitutions.persistence.entities.FinancialProductEntity;
import com.mitocode.financialinstitutions.persistence.repositories.FinancialInstitutionRepository;
import com.mitocode.financialinstitutions.persistence.repositories.FinancialProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Set;

@Service @RequiredArgsConstructor
public class FinancialProductService {
    private static final Set<String> ENABLED_INSTITUTION_CODES = Set.of("BCP", "BBVA", "INTERBANK");
    private final FinancialProductRepository repository;
    private final FinancialInstitutionRepository institutionRepository;

    @Transactional(readOnly = true)
    public Page<FinancialProductResource> findAll(String institution, Pageable pageable) {
        return repository.findByActiveTrueAndFinancialInstitution_CodeInAndFinancialInstitution_NameContainingIgnoreCase(ENABLED_INSTITUTION_CODES, institution, pageable).map(this::toResource);
    }
    @Transactional(readOnly = true)
    public FinancialProductResource findById(Integer id) { return toResource(requireActive(id)); }

    @Transactional
    public FinancialProductResource create(FinancialProductResource value) {
        validate(value);
        var entity = new FinancialProductEntity();
        apply(entity, value);
        entity.setVersion(value.getVersion() == null ? 1 : value.getVersion());
        return toResource(repository.save(entity));
    }

    @Transactional
    public FinancialProductResource createVersion(Integer currentId, FinancialProductResource value) {
        var current = requireActive(currentId);
        validate(value);
        current.setActive(false);
        current.setValidUntil(value.getValidFrom().minusDays(1));
        repository.save(current);
        var next = new FinancialProductEntity();
        apply(next, value);
        next.setVersion(current.getVersion() + 1);
        return toResource(repository.save(next));
    }

    @Transactional
    public void deactivate(Integer id) {
        var entity = requireActive(id);
        entity.setActive(false);
        repository.save(entity);
    }

    public FinancialProductEntity requireActive(Integer id) {
        var product = repository.findByIdAndActiveTrue(id).orElseThrow(() -> new ResourceNotFoundException("Producto financiero no encontrado"));
        if (!ENABLED_INSTITUTION_CODES.contains(product.getFinancialInstitution().getCode())) throw new ResourceNotFoundException("Producto financiero no encontrado");
        return product;
    }

    private void validate(FinancialProductResource v) {
        if (v.getMaxTermMonths() < v.getMinTermMonths()) throw new IllegalArgumentException("El plazo máximo debe ser mayor o igual al mínimo");
        if (v.getMaxDownPaymentPercent().compareTo(v.getMinDownPaymentPercent()) < 0) throw new IllegalArgumentException("La inicial máxima debe ser mayor o igual a la mínima");
        if (!Boolean.TRUE.equals(v.getBalloonAllowed()) && v.getMaxBalloonPercent().signum() != 0) throw new IllegalArgumentException("Un producto sin balón debe tener máximo 0%");
        if (v.getValidUntil() != null && v.getValidUntil().isBefore(v.getValidFrom())) throw new IllegalArgumentException("La vigencia final no puede anteceder a la inicial");
    }

    private void apply(FinancialProductEntity e, FinancialProductResource v) {
        e.setFinancialInstitution(institutionRepository.findById(v.getFinancialInstitutionId()).orElseThrow(() -> new ResourceNotFoundException("Entidad financiera no encontrada")));
        e.setProductName(v.getProductName().trim()); e.setCurrency(v.getCurrency()); e.setTeaPercent(v.getTeaPercent());
        e.setMinTermMonths(v.getMinTermMonths()); e.setMaxTermMonths(v.getMaxTermMonths());
        e.setMinDownPaymentPercent(v.getMinDownPaymentPercent()); e.setMaxDownPaymentPercent(v.getMaxDownPaymentPercent());
        e.setBalloonAllowed(v.getBalloonAllowed()); e.setMaxBalloonPercent(v.getMaxBalloonPercent());
        e.setCreditLifeInsuranceMonthlyPercent(v.getCreditLifeInsuranceMonthlyPercent()); e.setVehicleInsuranceAnnualPercent(v.getVehicleInsuranceAnnualPercent());
        e.setMonthlyFee(v.getMonthlyFee()); e.setAdminCost(v.getAdminCost()); e.setNotaryCost(v.getNotaryCost()); e.setOtherUpfrontCost(v.getOtherUpfrontCost());
        e.setCapitalizeInterestTotalGrace(v.getCapitalizeInterestTotalGrace()); e.setCapitalizeInsuranceTotalGrace(v.getCapitalizeInsuranceTotalGrace());
        e.setValidFrom(v.getValidFrom()); e.setValidUntil(v.getValidUntil()); e.setActive(true);
    }

    private FinancialProductResource toResource(FinancialProductEntity e) {
        var r = new FinancialProductResource();
        r.setId(e.getId()); r.setFinancialInstitutionId(e.getFinancialInstitution().getId()); r.setInstitutionCode(e.getFinancialInstitution().getCode());
        r.setInstitutionName(e.getFinancialInstitution().getName()); r.setProductName(e.getProductName()); r.setVersion(e.getVersion()); r.setCurrency(e.getCurrency());
        r.setTeaPercent(e.getTeaPercent()); r.setMinTermMonths(e.getMinTermMonths()); r.setMaxTermMonths(e.getMaxTermMonths());
        r.setMinDownPaymentPercent(e.getMinDownPaymentPercent()); r.setMaxDownPaymentPercent(e.getMaxDownPaymentPercent());
        r.setBalloonAllowed(e.getBalloonAllowed()); r.setMaxBalloonPercent(e.getMaxBalloonPercent());
        r.setCreditLifeInsuranceMonthlyPercent(e.getCreditLifeInsuranceMonthlyPercent()); r.setVehicleInsuranceAnnualPercent(e.getVehicleInsuranceAnnualPercent());
        r.setMonthlyFee(e.getMonthlyFee()); r.setAdminCost(e.getAdminCost()); r.setNotaryCost(e.getNotaryCost()); r.setOtherUpfrontCost(e.getOtherUpfrontCost());
        r.setCapitalizeInterestTotalGrace(e.getCapitalizeInterestTotalGrace()); r.setCapitalizeInsuranceTotalGrace(e.getCapitalizeInsuranceTotalGrace());
        r.setValidFrom(e.getValidFrom()); r.setValidUntil(e.getValidUntil()); r.setActive(e.getActive()); return r;
    }
}
