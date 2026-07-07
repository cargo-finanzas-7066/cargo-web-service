package com.mitocode.financialinstitutions.services.implementations;

import com.mitocode.financialinstitutions.persistence.entities.FinancialProductEntity;
import com.mitocode.financialinstitutions.persistence.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Component @Profile("dev") @Order(20) @RequiredArgsConstructor
public class FinancialProductSeedService implements CommandLineRunner {
    private static final Set<String> ENABLED_CODES = Set.of("BCP", "BBVA", "INTERBANK", "SCOTIABANK");
    private final FinancialInstitutionRepository institutions;
    private final FinancialProductRepository products;
    @Override public void run(String... args) {
        products.findAll().stream()
                .filter(p -> p.getFinancialInstitution() != null && p.getFinancialInstitution().getCode() != null && !ENABLED_CODES.contains(p.getFinancialInstitution().getCode()))
                .forEach(p -> { p.setActive(false); products.save(p); });
        institutions.findAll().stream().filter(i -> i.getCode()!=null && ENABLED_CODES.contains(i.getCode())).forEach(i -> {
            var existing = products.findFirstByFinancialInstitutionIdAndActiveTrueOrderByVersionDesc(i.getId());
            var p=existing.orElseGet(FinancialProductEntity::new);
            p.setFinancialInstitution(i);p.setProductName(i.getProduct()==null?"Crédito vehicular":i.getProduct());
            if (p.getVersion()==null) p.setVersion(1);
            p.setCurrency(i.getCurrency()==null?"PEN":i.getCurrency());p.setTeaPercent(BigDecimal.valueOf(i.getTea()==null?0:i.getTea()));
            p.setMinTermMonths(i.getMinTerm()==null?1:i.getMinTerm());p.setMaxTermMonths(i.getMaxTerm()==null?60:i.getMaxTerm());
            p.setMinDownPaymentPercent(BigDecimal.valueOf(i.getMinDownPayment()==null?0:i.getMinDownPayment()));p.setMaxDownPaymentPercent(new BigDecimal("100"));
            p.setBalloonAllowed(true);p.setMaxBalloonPercent(new BigDecimal("50"));p.setCreditLifeInsuranceMonthlyPercent(BigDecimal.valueOf(i.getInsuranceDisbursement()==null?0:i.getInsuranceDisbursement()));
            p.setVehicleInsuranceAnnualPercent(BigDecimal.valueOf(i.getInsuranceVehicle()==null?0:i.getInsuranceVehicle()));p.setMonthlyFee(BigDecimal.valueOf(i.getMonthlyFee()==null?0:i.getMonthlyFee()));
            p.setAdminCost(BigDecimal.valueOf(i.getAdminCost()==null?0:i.getAdminCost()));p.setNotaryCost(BigDecimal.ZERO);p.setOtherUpfrontCost(BigDecimal.ZERO);
            p.setCapitalizeInterestTotalGrace(true);p.setCapitalizeInsuranceTotalGrace(true);p.setValidFrom(LocalDate.now());p.setActive(true);products.save(p);
        });
    }
}
