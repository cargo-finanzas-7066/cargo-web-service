package com.mitocode.service;

import com.mitocode.dto.SimulationRequest;
import com.mitocode.dto.SimulationResult;
import com.mitocode.entity.PaymentScheduleEntity;
import com.mitocode.entity.SimulationEntity;
import com.mitocode.repository.PaymentScheduleRepository;
import com.mitocode.repository.SimulationRepository;
import com.mitocode.financialinstitutions.persistence.repositories.FinancialInstitutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SimulationService {
    private final SimulationRepository simRepo;
    private final PaymentScheduleRepository scheduleRepo;
    private final FinancialEngine engine;
    private final FinancialInstitutionRepository financialInstitutionRepository;

    public List<SimulationEntity> findAll() {
        var unique = new LinkedHashMap<String, SimulationEntity>();
        simRepo.findAll().stream()
                .sorted(Comparator.comparing(SimulationEntity::getId, Comparator.nullsLast(Integer::compareTo)).reversed())
                .forEach(simulation -> unique.putIfAbsent(fingerprint(simulation), simulation));
        return unique.values().stream()
                .sorted(Comparator.comparing(SimulationEntity::getId, Comparator.nullsLast(Integer::compareTo)).reversed())
                .toList();
    }
    public SimulationEntity findById(Integer id) { return simRepo.findById(id).orElseThrow(); }

    @Transactional
    public SimulationEntity save(SimulationRequest req) {
        SimulationEntity entity = req.getId() != null ? simRepo.findById(req.getId()).orElse(new SimulationEntity()) : new SimulationEntity();
        entity.setClientId(req.getClientId());
        entity.setVehicleId(req.getVehicleId());
        entity.setEntityId(req.getEntityId());
        entity.setCurrency(req.getCurrency());
        double vehiclePrice = value(req.getVehiclePrice());
        double downPayment = value(req.getDownPayment());
        double downPaymentPercent = req.getDownPaymentPercent() != null
                ? req.getDownPaymentPercent()
                : (vehiclePrice == 0.0 ? 0.0 : (downPayment / vehiclePrice) * 100.0);
        double tea = value(req.getTea());
        entity.setVehiclePrice(vehiclePrice);
        entity.setDownPayment(downPayment);
        entity.setDownPaymentPercent(downPaymentPercent);
        entity.setFinancedAmount(vehiclePrice - downPayment);
        entity.setTerm(req.getTerm() == null ? 1 : req.getTerm());
        entity.setTea(tea);
        entity.setTem((Math.pow(1 + tea / 100.0, 1.0 / 12.0) - 1.0) * 100.0);
        entity.setPaymentDay(req.getPaymentDay());
        entity.setDisbursementDate(req.getDisbursementDate() == null ? LocalDate.now() : req.getDisbursementDate());
        entity.setGraceType(normalizeGrace(req.getGraceType()));
        entity.setGraceMonths(req.getGraceMonths() == null ? 0 : req.getGraceMonths());
        entity.setBalloonEnabled(Boolean.TRUE.equals(req.getBalloonEnabled()));
        entity.setBalloonAmount(value(req.getBalloonAmount()));
        entity.setInsuranceDisbursement(value(req.getInsuranceDisbursement()));
        entity.setInsuranceVehicle(value(req.getInsuranceVehicle()));
        entity.setMonthlyFee(isBcp(req.getEntityId()) ? 0.0 : value(req.getMonthlyFee()));
        entity.setAdminCost(value(req.getAdminCost()));
        entity.setNotaryCost(value(req.getNotaryCost()));
        entity.setOtherCharges(value(req.getOtherCharges()));
        entity.setStatus(req.getStatus());
        if (entity.getCode() == null) {
            entity.setCreatedAt(LocalDate.now());
        }
        entity = simRepo.save(entity);
        // generate code
        if (entity.getCode() == null) {
            entity.setCode(String.format("SIM-%04d", entity.getId()));
            entity = simRepo.save(entity);
        }
        return entity;
    }

    @Transactional
    public SimulationResult calculate(Integer simulationId) {
        SimulationEntity sim = findById(simulationId);
        scheduleRepo.deleteBySimulationId(simulationId);
        double vehiclePrice = value(sim.getVehiclePrice());
        double downPayment = value(sim.getDownPayment());
        double downPaymentPercent = sim.getDownPaymentPercent() != null
                ? sim.getDownPaymentPercent()
                : (vehiclePrice == 0.0 ? 0.0 : (downPayment / vehiclePrice) * 100.0);
        SimulationResult result = engine.calculate(
                vehiclePrice, downPaymentPercent, value(sim.getTea()),
                sim.getTerm() == null ? 1 : sim.getTerm(), Boolean.TRUE.equals(sim.getBalloonEnabled()), value(sim.getBalloonAmount()),
                sim.getGraceType(), sim.getGraceMonths() == null ? 0 : sim.getGraceMonths(),
                value(sim.getInsuranceDisbursement()), value(sim.getInsuranceVehicle()),
                value(sim.getMonthlyFee()), sim.getDisbursementDate(), isBcp(sim.getEntityId())
        );

        List<PaymentScheduleEntity> schedules = result.getSchedule().stream().map(r -> {
            PaymentScheduleEntity pse = new PaymentScheduleEntity();
            pse.setSimulationId(simulationId);
            pse.setPeriod(r.getPeriod());
            pse.setDate(r.getDate());
            pse.setInitialBalance(r.getInitialBalance());
            pse.setPayment(r.getPayment());
            pse.setBalloonPayment(r.getBalloonPayment());
            pse.setInterest(r.getInterest());
            pse.setAmortization(r.getAmortization());
            pse.setInsurance(r.getInsurance());
            pse.setCommission(r.getCommission());
            pse.setTotalPayment(r.getTotalPayment());
            pse.setFinalBalance(r.getFinalBalance());
            return pse;
        }).collect(Collectors.toList());
        scheduleRepo.saveAll(schedules);

        sim.setFinancedAmount(result.getFinancedAmount());
        sim.setDownPaymentPercent(downPaymentPercent);
        sim.setBalloonEnabled(Boolean.TRUE.equals(sim.getBalloonEnabled()));
        sim.setBalloonAmount(value(sim.getBalloonAmount()));
        sim.setTem(result.getTem());
        sim.setMonthlyPayment(result.getMonthlyPayment());
        sim.setVan(result.getVan());
        sim.setTir(result.getTir());
        sim.setTcea(result.getTcea());
        sim.setStatus("Simulado");
        simRepo.save(sim);

        return result;
    }

    public void delete(Integer id) { simRepo.deleteById(id); }

    private boolean isBcp(Integer entityId) {
        if (entityId == null) {
            return false;
        }
        return financialInstitutionRepository.findById(entityId)
                .map(entity -> "BCP".equalsIgnoreCase(entity.getCode()) || entity.getShortName().toUpperCase().contains("BCP"))
                .orElse(false);
    }

    private String normalizeGrace(String graceType) {
        if ("total".equalsIgnoreCase(graceType)) return "T";
        if ("partial".equalsIgnoreCase(graceType)) return "P";
        if ("none".equalsIgnoreCase(graceType)) return "S";
        return graceType == null ? "S" : graceType;
    }

    private double value(Double value) {
        return value == null ? 0.0 : value;
    }

    private String fingerprint(SimulationEntity simulation) {
        double vehiclePrice = value(simulation.getVehiclePrice());
        double downPayment = value(simulation.getDownPayment());
        double downPaymentPercent = simulation.getDownPaymentPercent() != null
                ? simulation.getDownPaymentPercent()
                : (vehiclePrice == 0.0 ? 0.0 : (downPayment / vehiclePrice) * 100.0);
        return String.join("|",
                String.valueOf(simulation.getClientId()),
                String.valueOf(simulation.getVehicleId()),
                String.valueOf(simulation.getEntityId()),
                String.valueOf(moneyKey(vehiclePrice)),
                String.valueOf(moneyKey(downPayment)),
                String.valueOf(rateKey(downPaymentPercent)),
                String.valueOf(simulation.getTerm()),
                String.valueOf(rateKey(value(simulation.getTea()))),
                String.valueOf(simulation.getDisbursementDate()),
                String.valueOf(normalizeGrace(simulation.getGraceType())),
                String.valueOf(simulation.getGraceMonths() == null ? 0 : simulation.getGraceMonths()),
                String.valueOf(Boolean.TRUE.equals(simulation.getBalloonEnabled())),
                String.valueOf(moneyKey(value(simulation.getBalloonAmount())))
        );
    }

    private long moneyKey(double value) {
        return Math.round(value * 100.0);
    }

    private long rateKey(double value) {
        return Math.round(value * 10_000.0);
    }
}
