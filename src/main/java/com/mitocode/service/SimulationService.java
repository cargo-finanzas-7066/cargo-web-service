package com.mitocode.service;

import com.mitocode.customers.persistence.entities.CustomerEntity;
import com.mitocode.customers.persistence.repositories.CustomerRepository;
import com.mitocode.dto.*;
import com.mitocode.entity.*;
import com.mitocode.exception.*;
import com.mitocode.financialinstitutions.persistence.entities.FinancialProductEntity;
import com.mitocode.financialinstitutions.persistence.repositories.FinancialProductRepository;
import com.mitocode.iam.persistence.entities.Role;
import com.mitocode.iam.persistence.entities.UserEntity;
import com.mitocode.iam.services.implementations.CurrentUserService;
import com.mitocode.repository.*;
import com.mitocode.vehicles.persistence.entities.VehicleEntity;
import com.mitocode.vehicles.persistence.repositories.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.*;
import java.time.LocalDate;
import java.util.*;

@Service @RequiredArgsConstructor
public class SimulationService {
    private final SimulationRepository simulationRepository;
    private final PaymentScheduleRepository scheduleRepository;
    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final FinancialProductRepository productRepository;
    private final CurrentUserService currentUserService;
    private final FinancialEngine engine;

    @Transactional(readOnly = true)
    public List<QuoteResource> quote(QuoteRequest request) {
        UserEntity user = currentUserService.requireUser();
        CustomerEntity client = requireClient(request.getClientId(), user);
        VehicleEntity vehicle = vehicleRepository.findByIdAndActiveTrue(request.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado"));
        return request.getFinancialProductIds().stream().distinct().map(productId -> {
            FinancialProductEntity product = requireProduct(productId);
            var input = input(request.getVehiclePrice(), vehicle, request.getDownPaymentPercent(), request.getBalloonPercent(),
                    request.getCokTeaPercent(), request.getTermMonths(), request.getGraceType(), request.getGraceMonths(),
                    request.getFirstPaymentDate(), request.getPaymentDay(), product);
            var result = engine.calculate(input);
            var institution = product.getFinancialInstitution();
            return new QuoteResource(product.getId(), institution.getCode(), institution.getName(), product.getProductName(), product.getVersion(), result);
        }).toList();
    }

    @Transactional
    public SimulationResource save(SimulationRequest request) {
        UserEntity user = currentUserService.requireUser();
        requireClient(request.getClientId(), user);
        VehicleEntity vehicle = vehicleRepository.findByIdAndActiveTrue(request.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado"));
        FinancialProductEntity product = requireProduct(request.getFinancialProductId());
        var input = input(request.getVehiclePrice(), vehicle, request.getDownPaymentPercent(), request.getBalloonPercent(),
                request.getCokTeaPercent(), request.getTermMonths(), request.getGraceType(), request.getGraceMonths(),
                request.getFirstPaymentDate(), request.getPaymentDay(), product);
        SimulationResult result = engine.calculate(input);

        var entity = new SimulationEntity();
        entity.setOwner(user); entity.setClientId(request.getClientId()); entity.setVehicleId(request.getVehicleId());
        entity.setEntityId(product.getFinancialInstitution().getId()); entity.setFinancialProduct(product);
        entity.setProductSnapshot(snapshot(product)); entity.setCurrency(product.getCurrency()); entity.setVehiclePrice(input.vehiclePrice());
        entity.setDownPaymentPercent(input.downPaymentPercent());
        entity.setDownPayment(input.vehiclePrice().multiply(input.downPaymentPercent()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));
        entity.setFinancedAmount(result.getFinancedAmount()); entity.setTerm(input.termMonths()); entity.setFirstPaymentDate(input.firstPaymentDate());
        entity.setDisbursementDate(input.firstPaymentDate().minusMonths(1)); entity.setPaymentDay(input.paymentDay());
        entity.setGraceType(input.graceType().name()); entity.setGraceMonths(input.graceMonths());
        entity.setBalloonEnabled(input.balloonPercent().signum() > 0); entity.setBalloonPercent(input.balloonPercent()); entity.setBalloonAmount(result.getBalloonAmount());
        entity.setTea(result.getTea()); entity.setTem(result.getTem()); entity.setCokTea(result.getCokTeaPercent()); entity.setCokTem(result.getCokTemPercent()); entity.setMonthlyPayment(result.getMonthlyPayment());
        entity.setVan(result.getVan()); entity.setTir(result.getTir()); entity.setTcea(result.getTcea());
        entity.setTotalInterest(result.getTotalInterest()); entity.setTotalInsurance(result.getTotalInsurance());
        entity.setTotalFees(result.getTotalCommissions()); entity.setTotalPayment(result.getTotalPayment()); entity.setStatus("Guardado"); entity.setCreatedAt(LocalDate.now());
        entity = simulationRepository.save(entity);
        entity.setCode("SIM-%06d".formatted(entity.getId()));
        entity = simulationRepository.save(entity);

        Integer id = entity.getId();
        var periods = result.getSchedule().stream().map(row -> period(id, row)).toList();
        scheduleRepository.saveAll(periods);
        return toResource(entity, result.getSchedule());
    }

    @Transactional(readOnly = true)
    public Page<SimulationResource> findAll(Pageable pageable) {
        UserEntity user = currentUserService.requireUser();
        Page<SimulationEntity> page = user.getRole() == Role.ADMIN ? simulationRepository.findByArchivedFalse(pageable)
                : simulationRepository.findByOwnerIdAndArchivedFalse(user.getId(), pageable);
        return page.map(entity -> toResource(entity, null));
    }

    @Transactional(readOnly = true)
    public SimulationResource findById(Integer id) {
        UserEntity user = currentUserService.requireUser();
        var entity = accessible(id, user);
        var rows = scheduleRepository.findBySimulationIdOrderByPeriod(id).stream().map(this::row).toList();
        return toResource(entity, rows);
    }

    @Transactional
    public void archive(Integer id) {
        var entity = accessible(id, currentUserService.requireUser());
        entity.setArchived(true); entity.setStatus("Archivado"); simulationRepository.save(entity);
    }

    private SimulationEntity accessible(Integer id, UserEntity user) {
        var value = user.getRole() == Role.ADMIN ? simulationRepository.findByIdAndArchivedFalse(id)
                : simulationRepository.findByIdAndOwnerIdAndArchivedFalse(id, user.getId());
        return value.orElseThrow(() -> new ResourceNotFoundException("Simulación no encontrada"));
    }
    private CustomerEntity requireClient(Integer id, UserEntity user) {
        var value = user.getRole() == Role.ADMIN ? customerRepository.findByIdAndArchivedFalse(id)
                : customerRepository.findByIdAndOwnerIdAndArchivedFalse(id, user.getId());
        return value.orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
    }
    private FinancialProductEntity requireProduct(Integer id) {
        var p = productRepository.findByIdAndActiveTrue(id).orElseThrow(() -> new ResourceNotFoundException("Producto financiero no encontrado"));
        LocalDate today = LocalDate.now();
        if (p.getValidFrom().isAfter(today) || (p.getValidUntil()!=null && p.getValidUntil().isBefore(today))) throw new UnprocessableEntityException("El producto financiero no está vigente");
        return p;
    }
    private FinancialEngine.Input input(BigDecimal requestedPrice, VehicleEntity vehicle, BigDecimal down, BigDecimal balloon, BigDecimal cokTea,
                                        int term, GraceType grace, int graceMonths, LocalDate first, int paymentDay, FinancialProductEntity product) {
        BigDecimal price = requestedPrice == null ? vehicle.getPrice() : requestedPrice;
        if (!product.getCurrency().equalsIgnoreCase(vehicle.getCurrency())) throw new UnprocessableEntityException("La moneda del vehículo no coincide con el producto");
        return new FinancialEngine.Input(price, down, balloon, cokTea, term, grace, graceMonths, first, paymentDay, product);
    }
    private Map<String,Object> snapshot(FinancialProductEntity p) {
        var i=p.getFinancialInstitution(); var map=new LinkedHashMap<String,Object>();
        map.put("financialProductId",p.getId()); map.put("institutionCode",i.getCode()); map.put("institutionName",i.getName());
        map.put("productName",p.getProductName()); map.put("version",p.getVersion()); map.put("currency",p.getCurrency());
        map.put("teaPercent",p.getTeaPercent()); map.put("creditLifeInsuranceMonthlyPercent",p.getCreditLifeInsuranceMonthlyPercent());
        map.put("vehicleInsuranceAnnualPercent",p.getVehicleInsuranceAnnualPercent()); map.put("monthlyFee",p.getMonthlyFee());
        map.put("adminCost",p.getAdminCost()); map.put("notaryCost",p.getNotaryCost()); map.put("otherUpfrontCost",p.getOtherUpfrontCost()); return map;
    }
    private PaymentScheduleEntity period(Integer simulationId, PaymentRow r) {
        var e=new PaymentScheduleEntity(); e.setSimulationId(simulationId); e.setPeriod(r.getPeriod()); e.setDate(r.getDate());
        e.setInitialBalance(r.getInitialBalance()); e.setPayment(r.getPayment()); e.setBalloonPayment(r.getBalloonPayment());
        e.setInterest(r.getInterest()); e.setAmortization(r.getAmortization()); e.setInsurance(r.getInsurance()); e.setCommission(r.getCommission());
        e.setTotalPayment(r.getTotalPayment()); e.setFinalFlow(r.getFinalFlow()); e.setBaseFlow(r.getBaseFlow());
        e.setFinalBalance(r.getFinalBalance()); e.setGraceType(r.getGraceType()); return e;
    }
    private PaymentRow row(PaymentScheduleEntity e) {
        var r=new PaymentRow(); r.setPeriod(e.getPeriod());r.setDate(e.getDate());r.setInitialBalance(e.getInitialBalance());r.setPayment(e.getPayment());
        r.setBalloonPayment(e.getBalloonPayment());r.setInterest(e.getInterest());r.setAmortization(e.getAmortization());r.setInsurance(e.getInsurance());
        r.setCommission(e.getCommission());r.setTotalPayment(e.getTotalPayment());r.setFinalFlow(e.getFinalFlow());r.setBaseFlow(e.getBaseFlow());
        r.setFinalBalance(e.getFinalBalance());r.setGraceType(e.getGraceType());return r;
    }
    private SimulationResource toResource(SimulationEntity e, List<PaymentRow> schedule) {
        var r=new SimulationResource(); r.setId(e.getId());r.setCode(e.getCode());r.setClientId(e.getClientId());r.setVehicleId(e.getVehicleId());
        r.setFinancialProductId(e.getFinancialProduct()==null?null:e.getFinancialProduct().getId());r.setCurrency(e.getCurrency());r.setVehiclePrice(e.getVehiclePrice());
        r.setDownPaymentPercent(e.getDownPaymentPercent());r.setFinancedAmount(e.getFinancedAmount());r.setTermMonths(e.getTerm());r.setFirstPaymentDate(e.getFirstPaymentDate());
        r.setPaymentDay(e.getPaymentDay());r.setGraceType(e.getGraceType());r.setGraceMonths(e.getGraceMonths());r.setBalloonPercent(e.getBalloonPercent());
        r.setMonthlyPayment(e.getMonthlyPayment());r.setTeaPercent(e.getTea());r.setTemPercent(e.getTem());r.setCokTeaPercent(e.getCokTea());r.setCokTemPercent(e.getCokTem());r.setTirPercent(e.getTir());r.setTceaPercent(e.getTcea());r.setVan(e.getVan());
        r.setTotalInterest(e.getTotalInterest());r.setTotalInsurance(e.getTotalInsurance());r.setTotalFees(e.getTotalFees());r.setTotalPayment(e.getTotalPayment());
        r.setProductSnapshot(e.getProductSnapshot());r.setCreatedAt(e.getCreatedAtTimestamp());r.setSchedule(schedule);return r;
    }
}
