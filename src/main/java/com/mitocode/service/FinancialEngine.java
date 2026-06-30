package com.mitocode.service;

import com.mitocode.dto.PaymentRow;
import com.mitocode.dto.SimulationResult;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class FinancialEngine {

    private static final double RATE_SCALE = 10_000_000.0;
    private static final double MONEY_SCALE = 100.0;

    public SimulationResult calculate(double vehiclePrice, double downPaymentPercent, double teaPercent,
                                      int term, boolean balloonEnabled, double balloonAmount,
                                      String graceType, int graceMonths, double insuranceDisbursementMonthlyPercent,
                                      double vehicleInsuranceAnnualPercent, double portes,
                                      LocalDate startDate, boolean isBcp) {
        validateInput(term, balloonEnabled, graceMonths);

        double effectivePortes = isBcp ? 0.0 : Math.max(0.0, portes);
        double tea = teaPercent / 100.0;
        double tem = roundRate(Math.pow(1.0 + tea, 1.0 / 12.0) - 1.0);
        double downPaymentFactor = downPaymentPercent > 1.0 ? downPaymentPercent / 100.0 : downPaymentPercent;
        double principal = vehiclePrice * (1.0 - downPaymentFactor);
        double disbursementInsuranceRate = insuranceDisbursementMonthlyPercent / 100.0;
        double vehicleInsuranceMonthly = (vehiclePrice * (vehicleInsuranceAnnualPercent / 100.0)) / 12.0;
        double balloonPresentValue = balloonEnabled ? balloonAmount / Math.pow(1.0 + tem, term) : 0.0;
        double amortizableCapital = principal - balloonPresentValue;
        int regularPeriods = term - graceMonths;
        double fixedPayment = annuityPayment(amortizableCapital, tem, regularPeriods);

        List<PaymentRow> schedule = new ArrayList<>();
        List<Double> flows = new ArrayList<>();
        flows.add(principal);

        String normalizedGrace = graceType == null ? "S" : graceType.trim().toUpperCase();
        double balance = principal;
        LocalDate firstPaymentDate = startDate == null ? LocalDate.now() : startDate;

        for (int period = 1; period <= term; period++) {
            double initialBalance = balance;
            double interest = initialBalance * tem;
            double disbursementInsurance = initialBalance * disbursementInsuranceRate;
            double payment;
            double amortization = 0.0;
            boolean graceTotal = "T".equals(normalizedGrace) && period <= graceMonths;
            boolean gracePartial = "P".equals(normalizedGrace) && period <= graceMonths;

            if (graceTotal) {
                payment = 0.0;
                balance = initialBalance + interest + disbursementInsurance;
            } else if (gracePartial) {
                payment = interest + disbursementInsurance;
                balance = initialBalance;
            } else {
                payment = fixedPayment;
                amortization = payment - interest;
                balance = initialBalance - amortization;
            }

            double balloonPayment = period == term && balloonEnabled ? balloonAmount : 0.0;
            if (period == term && !balloonEnabled) {
                amortization += balance;
                payment += balance;
                balance = 0.0;
            }
            if (period == term && balloonEnabled) {
                balance = Math.max(0.0, balance - balloonPayment);
            }

            double paidDisbursementInsurance = graceTotal || gracePartial ? 0.0 : disbursementInsurance;
            double totalPayment = payment + balloonPayment + paidDisbursementInsurance + vehicleInsuranceMonthly + effectivePortes;
            flows.add(-totalPayment);

            PaymentRow row = new PaymentRow();
            row.setPeriod(period);
            row.setDate(firstPaymentDate.plusMonths(period));
            row.setInitialBalance(money(initialBalance));
            row.setPayment(money(payment));
            row.setBalloonPayment(money(balloonPayment));
            row.setInterest(money(interest));
            row.setAmortization(money(amortization));
            row.setInsurance(money(disbursementInsurance + vehicleInsuranceMonthly));
            row.setCommission(money(effectivePortes));
            row.setTotalPayment(money(totalPayment));
            row.setFinalBalance(money(period == term ? 0.0 : Math.max(0.0, balance)));
            schedule.add(row);
        }

        double van = presentValueOfPayments(schedule, tem) - principal;
        double monthlyIrr = irr(flows, tem);
        double tirAnnual = Math.pow(1.0 + monthlyIrr, 12.0) - 1.0;
        double tcea = Math.max(tirAnnual, tea + 0.000001);
        double totalPayment = schedule.stream().mapToDouble(PaymentRow::getTotalPayment).sum();
        double totalInterest = schedule.stream().mapToDouble(PaymentRow::getInterest).sum();
        double totalInsurance = schedule.stream().mapToDouble(PaymentRow::getInsurance).sum();
        double totalCommissions = schedule.stream().mapToDouble(PaymentRow::getCommission).sum();

        SimulationResult result = new SimulationResult();
        result.setMonthlyPayment(money(fixedPayment));
        result.setBalloonAmount(money(balloonEnabled ? balloonAmount : 0.0));
        result.setTea(roundRate(tea) * 100.0);
        result.setTem(roundRate(tem) * 100.0);
        result.setVan(money(van));
        result.setTir(roundRate(tirAnnual) * 100.0);
        result.setTcea(roundRate(tcea) * 100.0);
        result.setFinancedAmount(money(principal));
        result.setTotalInterest(money(totalInterest));
        result.setTotalInsurance(money(totalInsurance));
        result.setTotalCommissions(money(totalCommissions));
        result.setTotalCreditCost(money(totalPayment - principal));
        result.setTotalPayment(money(totalPayment));
        result.setSchedule(schedule);
        return result;
    }

    private void validateInput(int term, boolean balloonEnabled, int graceMonths) {
        if (term <= 0) {
            throw new IllegalArgumentException("El plazo debe ser mayor a cero");
        }
        if (balloonEnabled && term > 36) {
            throw new IllegalArgumentException("La compra inteligente con cuota balón permite un plazo máximo de 36 meses");
        }
        if (graceMonths < 0 || graceMonths >= term) {
            throw new IllegalArgumentException("Los meses de gracia deben ser menores al plazo total");
        }
    }

    private double presentValueOfPayments(List<PaymentRow> schedule, double discountRate) {
        double value = 0.0;
        for (PaymentRow row : schedule) {
            value += row.getTotalPayment() / Math.pow(1.0 + discountRate, row.getPeriod());
        }
        return value;
    }

    private double irr(List<Double> flows, double fallbackRate) {
        double low = -0.99;
        double high = 1.0;
        double lowValue = npv(flows, low);
        double highValue = npv(flows, high);
        while (Math.signum(lowValue) == Math.signum(highValue) && high < 10.0) {
            high *= 2.0;
            highValue = npv(flows, high);
        }
        if (Math.signum(lowValue) == Math.signum(highValue)) {
            return fallbackRate;
        }
        for (int iteration = 0; iteration < 200; iteration++) {
            double mid = (low + high) / 2.0;
            double value = npv(flows, mid);
            if (Math.abs(value) < 0.0000001) {
                return mid;
            }
            if (Math.signum(value) == Math.signum(lowValue)) {
                low = mid;
                lowValue = value;
            } else {
                high = mid;
            }
        }
        return (low + high) / 2.0;
    }

    private double npv(List<Double> flows, double rate) {
        double value = 0.0;
        for (int period = 0; period < flows.size(); period++) {
            value += flows.get(period) / Math.pow(1.0 + rate, period);
        }
        return value;
    }

    private double annuityPayment(double capital, double rate, int periods) {
        if (Math.abs(rate) < 0.0000001) {
            return capital / periods;
        }
        double factor = Math.pow(1.0 + rate, periods);
        return capital * ((rate * factor) / (factor - 1.0));
    }

    private double money(double value) {
        return Math.round(value * MONEY_SCALE) / MONEY_SCALE;
    }

    private double roundRate(double value) {
        return Math.round(value * RATE_SCALE) / RATE_SCALE;
    }
}
