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

@Component @Profile("dev") @Order(20) @RequiredArgsConstructor
public class FinancialProductSeedService implements CommandLineRunner {
    private final FinancialInstitutionRepository institutions;
    private final FinancialProductRepository products;
    @Override public void run(String... args) {
        institutions.findAll().stream().filter(i -> i.getCode()!=null && !products.existsByFinancialInstitutionId(i.getId())).forEach(i -> {
            var p=new FinancialProductEntity();p.setFinancialInstitution(i);p.setProductName(i.getProduct()==null?"Crédito vehicular":i.getProduct());p.setVersion(1);
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
