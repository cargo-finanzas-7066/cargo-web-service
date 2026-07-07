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
    void cokTeaIsConvertedToMonthlyRateForVan() {
        var result=engine.calculate(input(product("0"),"12000","0","0","12",12,GraceType.NONE,0));
        assertThat(result.getCokTeaPercent()).isEqualByComparingTo("12.0000000");
        assertThat(result.getCokTemPercent()).isEqualByComparingTo("0.9488793");
        assertThat(result.getVan()).isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    void vanUsesBaseCashflowBeforeBalloonPayment() {
        var result=engine.calculate(input(product("0"),"12000","0","50","12",12,GraceType.NONE,0));
        assertThat(result.getBalloonAmount()).isEqualByComparingTo("6000.00");
        assertThat(result.getVan()).isEqualByComparingTo("997.10");
    }

    @Test
    void balloonAndTotalGraceNeverHideResidualBalance() {
        var p=product("13.2"); p.setCreditLifeInsuranceMonthlyPercent(new BigDecimal("0.05"));
        var result=engine.calculate(input(p,"28500","25","35",36,GraceType.TOTAL,6));
        assertThat(result.getSchedule()).hasSize(36);
        assertThat(result.getSchedule().get(35).getFinalBalance()).isEqualByComparingTo("0.00");
        assertThat(result.getSchedule().get(35).getBalloonPayment()).isEqualByComparingTo("7481.25");
        assertThat(result.getSchedule().subList(0,6)).allMatch(row -> row.getGraceType().equals("TOTAL"));
        assertThat(result.getTotalInsurance()).isEqualByComparingTo("384.75");
    }

    @Test
    void excelModelIgnoresPortesAndUpfrontCosts() {
        var p=product("10"); p.setCreditLifeInsuranceMonthlyPercent(new BigDecimal("0.05"));
        p.setVehicleInsuranceAnnualPercent(new BigDecimal("3.5")); p.setMonthlyFee(new BigDecimal("12"));
        p.setAdminCost(new BigDecimal("100")); p.setNotaryCost(new BigDecimal("50")); p.setOtherUpfrontCost(new BigDecimal("25"));
        var result=engine.calculate(input(p,"30000","20","0",24,GraceType.PARTIAL,2));
        assertThat(result.getTcea()).isGreaterThan(result.getTea());
        assertThat(result.getTotalCommissions()).isEqualByComparingTo("0.00");
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

    @Test
    void matchesCapitalizandoReferenceWithCokAndBaseFlow() {
        var p=product("12.5");
        p.setCreditLifeInsuranceMonthlyPercent(new BigDecimal("0.05"));
        p.setVehicleInsuranceAnnualPercent(new BigDecimal("4.72"));

        var result=engine.calculate(input(p,"106915.50","20","50","13",33,GraceType.PARTIAL,3));

        assertThat(result.getFinancedAmount()).isEqualByComparingTo("85532.40");
        assertThat(result.getBalloonAmount()).isEqualByComparingTo("42766.20");
        assertThat(result.getMonthlyPayment()).isEqualByComparingTo("2129.38");
        assertThat(result.getCokTemPercent()).isEqualByComparingTo("1.0236844");
        assertThat(result.getVan()).isEqualByComparingTo("-636.43");
        assertThat(result.getTir()).isEqualByComparingTo("1.6439375");
        assertThat(result.getTcea()).isEqualByComparingTo("21.6123745");

        var grace=result.getSchedule().get(0);
        assertThat(grace.getInterest()).isEqualByComparingTo("843.66");
        assertThat(grace.getCreditLifeInsurance()).isEqualByComparingTo("42.77");
        assertThat(grace.getVehicleInsurance()).isEqualByComparingTo("420.50");
        assertThat(grace.getPayment()).isEqualByComparingTo("843.66");
        assertThat(grace.getFinalBalance()).isEqualByComparingTo("85995.66");

        var firstRegular=result.getSchedule().get(3);
        assertThat(firstRegular.getInterest()).isEqualByComparingTo("857.36");
        assertThat(firstRegular.getAmortization()).isEqualByComparingTo("1272.02");
        assertThat(firstRegular.getPayment()).isEqualByComparingTo("2592.65");
        assertThat(firstRegular.getBaseFlow()).isEqualByComparingTo("-2129.38");
        var last=result.getSchedule().get(32);
        assertThat(last.getAmortization()).isEqualByComparingTo(last.getInitialBalance());
        assertThat(last.getFinalBalance()).isEqualByComparingTo("0.00");
    }

    @Test
    void rejectsBalloonThatWouldProduceNegativeInstallments() {
        var p=product("12"); p.setMaxBalloonPercent(new BigDecimal("40"));
        assertThatThrownBy(() -> engine.calculate(input(p,"20000","90","50",12,GraceType.NONE,0)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("bal");
    }

    @Test
    void rejectsCokLowerThanProductTea() {
        var p=product("12");
        assertThatThrownBy(() -> engine.calculate(input(p,"20000","20","0","10",12,GraceType.NONE,0)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("COK");
    }

    @Test
    void upfrontCostsDoNotAffectCapitalizingSpreadsheetModel() {
        var p=product("12"); p.setAdminCost(new BigDecimal("2000"));
        var result=engine.calculate(input(p,"2000","0","0",12,GraceType.NONE,0));
        assertThat(result.getFinancedAmount()).isEqualByComparingTo("2000.00");
        assertThat(result.getTotalCommissions()).isEqualByComparingTo("0.00");
    }

    private FinancialEngine.Input input(FinancialProductEntity p,String price,String down,String balloon,int term,GraceType grace,int graceMonths){
        return input(p, price, down, balloon, p.getTeaPercent().toPlainString(), term, grace, graceMonths);
    }
    private FinancialEngine.Input input(FinancialProductEntity p,String price,String down,String balloon,String cokTea,int term,GraceType grace,int graceMonths){
        return new FinancialEngine.Input(new BigDecimal(price),p.getTeaPercent(),new BigDecimal(down),new BigDecimal(balloon),
                cokTea == null ? null : new BigDecimal(cokTea),term,grace,graceMonths,
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
