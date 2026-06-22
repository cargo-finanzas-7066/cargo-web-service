package com.mitocode.service;

import com.mitocode.dto.SimulationRequest;
import com.mitocode.dto.SimulationResult;
import com.mitocode.entity.PaymentScheduleEntity;
import com.mitocode.entity.SimulationEntity;
import com.mitocode.repository.PaymentScheduleRepository;
import com.mitocode.repository.SimulationRepository;
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
        entity.setFinancedAmount(req.getVehiclePrice() - req.getDownPayment());
        entity.setTerm(req.getTerm());
        entity.setTea(req.getTea());
        entity.setPaymentDay(req.getPaymentDay());
        entity.setDisbursementDate(req.getDisbursementDate());
        entity.setGraceType(req.getGraceType());
        entity.setGraceMonths(req.getGraceMonths());
        entity.setInsuranceDisbursement(req.getInsuranceDisbursement());
        entity.setInsuranceVehicle(req.getInsuranceVehicle());
        entity.setMonthlyFee(req.getMonthlyFee());
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
                sim.getFinancedAmount(), sim.getTerm(), sim.getTea(),
                sim.getGraceType(), sim.getGraceMonths(),
                sim.getInsuranceDisbursement(), sim.getInsuranceVehicle(),
                sim.getMonthlyFee(), sim.getDisbursementDate()
        );

        List<PaymentScheduleEntity> schedules = result.getSchedule().stream().map(r -> {
            PaymentScheduleEntity pse = new PaymentScheduleEntity();
            pse.setSimulationId(simulationId);
            pse.setPeriod(r.getPeriod());
            pse.setDate(r.getDate());
            pse.setInitialBalance(r.getInitialBalance());
            pse.setPayment(r.getPayment());
            pse.setInterest(r.getInterest());
            pse.setAmortization(r.getAmortization());
            pse.setInsurance(r.getInsurance());
            pse.setCommission(r.getCommission());
            pse.setFinalBalance(r.getFinalBalance());
            return pse;
        }).collect(Collectors.toList());
        scheduleRepo.saveAll(schedules);

        return result;
    }

    public void delete(Integer id) { simRepo.deleteById(id); }
}
