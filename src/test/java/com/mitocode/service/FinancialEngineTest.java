package com.mitocode.service;

import com.mitocode.dto.GraceType;
import com.mitocode.exception.UnprocessableEntityException;
import com.mitocode.financialinstitutions.persistence.entities.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.*;

class FinancialEngineTest {
    private final FinancialEngine engine = new FinancialEngine();

    @Test
    void zeroRateProducesExactFrenchSchedule() {
        var result=engine.calculate(input(product("0"),"12000","0","0",12,GraceType.NONE,0));
        assertThat(result.getMonthlyPayment()).isEqualByComparingTo("1000.00");
        assertThat(result.getSchedule()).hasSize(12);
        assertThat(result.getSchedule().get(11).getFinalBalance()).isEqualByComparingTo("0.00");
        assertThat(result.getTotalInterest()).isEqualByComparingTo("0.00");
    }

    @Test
    void balloonAndTotalGraceNeverHideResidualBalance() {
        var p=product("13.2"); p.setCreditLifeInsuranceMonthlyPercent(new BigDecimal("0.05"));
        var result=engine.calculate(input(p,"28500","25","35",36,GraceType.TOTAL,6));
        assertThat(result.getSchedule()).hasSize(36);
        assertThat(result.getSchedule().get(35).getFinalBalance()).isEqualByComparingTo("0.00");
        assertThat(result.getSchedule().get(35).getBalloonPayment()).isEqualByComparingTo("9975.00");
        assertThat(result.getSchedule().subList(0,6)).allMatch(row -> row.getGraceType().equals("TOTAL"));
    }

    @Test
    void allCostsAreIncludedInTceaAndTotals() {
        var p=product("10"); p.setCreditLifeInsuranceMonthlyPercent(new BigDecimal("0.05"));
        p.setVehicleInsuranceAnnualPercent(new BigDecimal("3.5")); p.setMonthlyFee(new BigDecimal("12"));
        p.setAdminCost(new BigDecimal("100")); p.setNotaryCost(new BigDecimal("50")); p.setOtherUpfrontCost(new BigDecimal("25"));
        var result=engine.calculate(input(p,"30000","20","0",24,GraceType.PARTIAL,2));
        assertThat(result.getTcea()).isGreaterThan(result.getTea());
        assertThat(result.getTotalCommissions()).isGreaterThan(new BigDecimal("175"));
        assertThat(result.getTotalInsurance()).isPositive();
        assertThat(result.getTotalPayment()).isGreaterThan(result.getFinancedAmount());
    }

    @Test
    void rejectsTermsAndBalloonOutsideProductRules() {
        var p=product("12"); p.setBalloonAllowed(false); p.setMaxBalloonPercent(BigDecimal.ZERO);
        assertThatThrownBy(() -> engine.calculate(input(p,"20000","20","10",12,GraceType.NONE,0)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("balón");
        assertThatThrownBy(() -> engine.calculate(input(p,"20000","20","0",72,GraceType.NONE,0)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("plazo");
    }

    private FinancialEngine.Input input(FinancialProductEntity p,String price,String down,String balloon,int term,GraceType grace,int graceMonths){
        return new FinancialEngine.Input(new BigDecimal(price),new BigDecimal(down),new BigDecimal(balloon),term,grace,graceMonths,
                LocalDate.of(2026,7,5),5,p);
    }
    private FinancialProductEntity product(String tea){
        var institution=new FinancialInstitutionEntity();institution.setId(1);institution.setCode("TEST");institution.setName("Banco Test");
        var p=new FinancialProductEntity();p.setId(1);p.setFinancialInstitution(institution);p.setProductName("Crédito vehicular");p.setVersion(1);p.setCurrency("PEN");
        p.setTeaPercent(new BigDecimal(tea));p.setMinTermMonths(1);p.setMaxTermMonths(60);p.setMinDownPaymentPercent(BigDecimal.ZERO);p.setMaxDownPaymentPercent(new BigDecimal("100"));
        p.setBalloonAllowed(true);p.setMaxBalloonPercent(new BigDecimal("50"));p.setCreditLifeInsuranceMonthlyPercent(BigDecimal.ZERO);p.setVehicleInsuranceAnnualPercent(BigDecimal.ZERO);
        p.setMonthlyFee(BigDecimal.ZERO);p.setAdminCost(BigDecimal.ZERO);p.setNotaryCost(BigDecimal.ZERO);p.setOtherUpfrontCost(BigDecimal.ZERO);
        p.setCapitalizeInterestTotalGrace(true);p.setCapitalizeInsuranceTotalGrace(true);p.setValidFrom(LocalDate.of(2026,1,1));p.setActive(true);return p;
    }
}
