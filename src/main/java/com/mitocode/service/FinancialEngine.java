package com.mitocode.service;

import com.mitocode.dto.PaymentRow;
import com.mitocode.dto.SimulationResult;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class FinancialEngine {

    public SimulationResult calculate(double financedAmount, int term, double tea,
                                      String graceType, int graceMonths,
                                      double insuranceDisbursement, double insuranceVehicle,
                                      double monthlyFee, LocalDate disbursementDate) {
        double monthlyRate = Math.pow(1 + tea / 100, 1.0 / 12) - 1;
        List<PaymentRow> schedule = new ArrayList<>();
        double balance = financedAmount;
        LocalDate date = disbursementDate;
        double totalPayment = 0;

        for (int i = 1; i <= term; i++) {
            date = date.plusMonths(1);
            double interest = balance * monthlyRate;
            double payment, amort = 0, ins = 0, comm = monthlyFee;

            if ("total".equals(graceType) && i <= graceMonths) {
                payment = monthlyFee;
                balance += interest;
            } else if ("partial".equals(graceType) && i <= graceMonths) {
                payment = interest + monthlyFee;
            } else {
                int remaining = term - i + 1;
                payment = (monthlyRate * balance) / (1 - Math.pow(1 + monthlyRate, -remaining));
                amort = payment - interest;
                balance -= amort;
            }

            ins = (insuranceDisbursement / 100) * financedAmount + (insuranceVehicle / 100) * financedAmount / 12;
            totalPayment += payment;

            PaymentRow row = new PaymentRow();
            row.setPeriod(i);
            row.setDate(date);
            row.setInitialBalance(Math.round((balance + amort + ("total".equals(graceType) && i <= graceMonths ? interest : 0)) * 100.0) / 100.0);
            row.setPayment(Math.round(payment * 100.0) / 100.0);
            row.setInterest(Math.round(interest * 100.0) / 100.0);
            row.setAmortization(Math.round(amort * 100.0) / 100.0);
            row.setInsurance(Math.round(ins * 100.0) / 100.0);
            row.setCommission(Math.round(comm * 100.0) / 100.0);
            row.setFinalBalance(Math.max(0, Math.round(balance * 100.0) / 100.0));
            schedule.add(row);
        }

        double totalInterest = schedule.stream().mapToDouble(PaymentRow::getInterest).sum();
        double totalCosts = schedule.stream().mapToDouble(r -> r.getInsurance() + r.getCommission()).sum();
        double tcea = (Math.pow(1 + (totalInterest + totalCosts) / financedAmount, 12.0 / term) - 1) * 100;
        double van = -financedAmount;
        for (int i = 0; i < schedule.size(); i++) {
            van += schedule.get(i).getPayment() / Math.pow(1 + monthlyRate, i + 1);
        }
        double tir = (Math.pow(1 + (totalInterest + totalCosts) / financedAmount, 12.0 / term) - 1) * 100;

        SimulationResult result = new SimulationResult();
        result.setMonthlyPayment(Math.round(schedule.get(0).getPayment() * 100.0) / 100.0);
        result.setVan(Math.round(van * 100.0) / 100.0);
        result.setTir(Math.round(tir * 100.0) / 100.0);
        result.setTcea(Math.round(tcea * 100.0) / 100.0);
        result.setFinancedAmount(financedAmount);
        result.setTotalPayment(Math.round((totalPayment + schedule.stream().mapToDouble(r -> r.getInsurance() + r.getCommission()).sum()) * 100.0) / 100.0);
        result.setSchedule(schedule);
        return result;
    }
}
