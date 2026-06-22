package com.mitocode.service;

import com.mitocode.dto.PaymentRow;
import com.mitocode.dto.SimulationResult;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class FinancialEngine {

    public SimulationResult calculate(double vehiclePrice, double downPaymentPercent, double teaPercent,
                                      int term, boolean balloonEnabled, double balloonAmount,
                                      String graceType, int graceMonths, double insuranceDisbursementMonthlyPercent,
                                      double vehicleInsuranceAnnualPercent, double portes,
                                      LocalDate startDate, boolean isBcp) {
        if (balloonEnabled && term > 36) {
            throw new IllegalArgumentException("La compra inteligente con cuota balón permite un plazo máximo de 36 meses");
        }

        double effectivePortes = isBcp ? 0.0 : portes;
        double tea = teaPercent / 100.0;
        double tem = roundRate(Math.pow(1.0 + tea, 1.0 / 12.0) - 1.0);
        double downPayment = vehiclePrice * (downPaymentPercent / 100.0);
        double principal = vehiclePrice * (1.0 - downPaymentPercent / 100.0);
        double vehicleInsuranceMonthly = (vehiclePrice * (vehicleInsuranceAnnualPercent / 100.0)) / 12.0;
        double balloonPresentValue = balloonEnabled ? balloonAmount / Math.pow(1.0 + tem, term) : 0.0;
        double amortizableCapital = principal - balloonPresentValue;
        int regularPeriods = Math.max(1, term - graceMonths);
        double fixedPayment = amortizableCapital * ((tem * Math.pow(1.0 + tem, regularPeriods)) / (Math.pow(1.0 + tem, regularPeriods) - 1.0));

        List<PaymentRow> schedule = new ArrayList<>();
        List<Double> flows = new ArrayList<>();
        flows.add(principal);

        double balance = principal;
        LocalDate paymentDate = startDate;

        for (int period = 1; period <= term; period++) {
            double initialBalance = balance;
            double interest = initialBalance * tem;
            double disbursementInsurance = initialBalance * (insuranceDisbursementMonthlyPercent / 100.0);
            double payment;
            double amortization = 0.0;

            if ("T".equalsIgnoreCase(graceType) && period <= graceMonths) {
                payment = 0.0;
                balance = initialBalance + interest + disbursementInsurance;
            } else if ("P".equalsIgnoreCase(graceType) && period <= graceMonths) {
                payment = interest + disbursementInsurance;
                balance = initialBalance;
            } else {
                payment = fixedPayment;
                amortization = payment - interest;
                balance = Math.max(0.0, initialBalance - amortization);
            }

            double balloonPayment = period == term && balloonEnabled ? balloonAmount : 0.0;
            if (period == term && !balloonEnabled) {
                amortization += balance;
                payment += balance;
                balance = 0.0;
            }

            double totalPayment = payment + balloonPayment + vehicleInsuranceMonthly + effectivePortes;
            flows.add(-totalPayment);

            PaymentRow row = new PaymentRow();
            row.setPeriod(period);
            row.setDate(paymentDate.plusMonths(period));
            row.setInitialBalance(money(initialBalance));
            row.setPayment(money(payment));
            row.setBalloonPayment(money(balloonPayment));
            row.setInterest(money(interest));
            row.setAmortization(money(amortization));
            row.setInsurance(money(vehicleInsuranceMonthly + disbursementInsurance));
            row.setCommission(money(effectivePortes));
            row.setTotalPayment(money(totalPayment));
            row.setFinalBalance(money(period == term ? 0.0 : balance));
            schedule.add(row);
        }

        double van = -principal;
        for (int i = 1; i < flows.size(); i++) {
            van += -flows.get(i) / Math.pow(1.0 + tem, i);
        }

        double monthlyIrr = irr(flows);
        double tirAnnual = Math.pow(1.0 + monthlyIrr, 12.0) - 1.0;
        double tcea = Math.max(tirAnnual, tea + 0.000001);
        double totalPayment = schedule.stream().mapToDouble(PaymentRow::getTotalPayment).sum();
        double totalInterest = schedule.stream().mapToDouble(PaymentRow::getInterest).sum();
        double totalInsurance = schedule.stream().mapToDouble(PaymentRow::getInsurance).sum();
        double totalCommissions = schedule.stream().mapToDouble(PaymentRow::getCommission).sum();

        SimulationResult result = new SimulationResult();
        result.setMonthlyPayment(money(schedule.stream().filter(r -> r.getPayment() > 0).findFirst().map(PaymentRow::getPayment).orElse(0.0)));
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

    private double irr(List<Double> flows) {
        double rate = 0.01;
        for (int iteration = 0; iteration < 100; iteration++) {
            double npv = 0.0;
            double derivative = 0.0;
            for (int t = 0; t < flows.size(); t++) {
                npv += flows.get(t) / Math.pow(1.0 + rate, t);
                if (t > 0) {
                    derivative -= t * flows.get(t) / Math.pow(1.0 + rate, t + 1);
                }
            }
            if (Math.abs(derivative) < 0.0000001) {
                break;
            }
            double next = rate - npv / derivative;
            if (Double.isNaN(next) || Double.isInfinite(next) || next <= -0.99) {
                break;
            }
            if (Math.abs(next - rate) < 0.0000001) {
                return next;
            }
            rate = next;
        }
        return rate;
    }

    private double money(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private double roundRate(double value) {
        return Math.round(value * 10_000_000.0) / 10_000_000.0;
    }
}
