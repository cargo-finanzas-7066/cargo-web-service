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
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SimulationService {
    private final SimulationRepository simRepo;
    private final PaymentScheduleRepository scheduleRepo;
    private final FinancialEngine engine;
    private final FinancialInstitutionRepository financialInstitutionRepository;

    public List<SimulationEntity> findAll() { return simRepo.findAll(); }
    public SimulationEntity findById(Integer id) { return simRepo.findById(id).orElseThrow(); }

    @Transactional
    public SimulationEntity save(SimulationRequest req) {
        SimulationEntity entity = req.getId() != null ? simRepo.findById(req.getId()).orElse(new SimulationEntity()) : new SimulationEntity();
        entity.setClientId(req.getClientId());
        entity.setVehicleId(req.getVehicleId());
        entity.setEntityId(req.getEntityId());
        entity.setCurrency(req.getCurrency());
        entity.setVehiclePrice(req.getVehiclePrice());
        entity.setDownPayment(req.getDownPayment());
        entity.setDownPaymentPercent(req.getDownPaymentPercent());
        entity.setFinancedAmount(req.getVehiclePrice() - req.getDownPayment());
        entity.setTerm(req.getTerm());
        entity.setTea(req.getTea());
        entity.setTem(Math.pow(1 + req.getTea() / 100.0, 1.0 / 12.0) - 1.0);
        entity.setPaymentDay(req.getPaymentDay());
        entity.setDisbursementDate(req.getDisbursementDate());
        entity.setGraceType(normalizeGrace(req.getGraceType()));
        entity.setGraceMonths(req.getGraceMonths());
        entity.setBalloonEnabled(Boolean.TRUE.equals(req.getBalloonEnabled()));
        entity.setBalloonAmount(req.getBalloonAmount() == null ? 0.0 : req.getBalloonAmount());
        entity.setInsuranceDisbursement(req.getInsuranceDisbursement());
        entity.setInsuranceVehicle(req.getInsuranceVehicle());
        entity.setMonthlyFee(isBcp(req.getEntityId()) ? 0.0 : req.getMonthlyFee());
        entity.setAdminCost(req.getAdminCost());
        entity.setNotaryCost(req.getNotaryCost());
        entity.setOtherCharges(req.getOtherCharges());
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

    public SimulationResult calculate(Integer simulationId) {
        SimulationEntity sim = findById(simulationId);
        scheduleRepo.deleteBySimulationId(simulationId);
        SimulationResult result = engine.calculate(
                sim.getVehiclePrice(), sim.getDownPaymentPercent(), sim.getTea(),
                sim.getTerm(), Boolean.TRUE.equals(sim.getBalloonEnabled()), sim.getBalloonAmount(),
                sim.getGraceType(), sim.getGraceMonths(),
                sim.getInsuranceDisbursement(), sim.getInsuranceVehicle(),
                sim.getMonthlyFee(), sim.getDisbursementDate(), isBcp(sim.getEntityId())
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
}
