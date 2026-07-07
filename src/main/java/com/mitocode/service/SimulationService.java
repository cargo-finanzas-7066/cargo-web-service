package com.mitocode.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.regex.Pattern;

@Service @RequiredArgsConstructor
public class SimulationService {
    private static final Set<String> ENABLED_INSTITUTION_CODES = Set.of("BCP", "BBVA", "INTERBANK");
    private static final Pattern RATE_NUMBER = Pattern.compile("\\d+(?:\\.\\d+)?");
    private final SimulationRepository simulationRepository;
    private final PaymentScheduleRepository scheduleRepository;
    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final FinancialProductRepository productRepository;
    private final CurrentUserService currentUserService;
    private final FinancialEngine engine;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public List<QuoteResource> quote(QuoteRequest request) {
        UserEntity user = currentUserService.requireUser();
        CustomerEntity client = requireClient(request.getClientId(), user);
        VehicleEntity vehicle = vehicleRepository.findByIdAndActiveTrue(request.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado"));
        return request.getFinancialProductIds().stream().distinct().map(productId -> {
            FinancialProductEntity product = requireProduct(productId);
            validateInsuranceRanges(Boolean.TRUE.equals(request.getCreditLifeInsuranceEnabled()) ? request.getCreditLifeInsuranceMonthlyPercent() : null,
                    Boolean.TRUE.equals(request.getVehicleInsuranceEnabled()) ? request.getVehicleInsuranceAnnualPercent() : null, product);
            var input = input(request.getVehiclePrice(), vehicle, request.getDownPaymentPercent(), request.getBalloonPercent(),
                    null, request.getCokTeaPercent(), request.getTermMonths(), request.getGraceType(), request.getGraceMonths(),
                    request.getFirstPaymentDate(), request.getPaymentDay(), request.getCreditLifeInsuranceEnabled(), request.getCreditLifeInsuranceMonthlyPercent(),
                    request.getVehicleInsuranceEnabled(), request.getVehicleInsuranceAnnualPercent(), product);
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
        validateTeaRange(request.getTeaPercent(), product);
        validateInsuranceRanges(Boolean.TRUE.equals(request.getCreditLifeInsuranceEnabled()) ? request.getCreditLifeInsuranceMonthlyPercent() : null,
                Boolean.TRUE.equals(request.getVehicleInsuranceEnabled()) ? request.getVehicleInsuranceAnnualPercent() : null, product);
        var input = input(request.getVehiclePrice(), vehicle, request.getDownPaymentPercent(), request.getBalloonPercent(),
                request.getTeaPercent(), request.getCokTeaPercent(), request.getTermMonths(), request.getGraceType(), request.getGraceMonths(),
                request.getFirstPaymentDate(), request.getPaymentDay(), request.getCreditLifeInsuranceEnabled(), request.getCreditLifeInsuranceMonthlyPercent(),
                request.getVehicleInsuranceEnabled(), request.getVehicleInsuranceAnnualPercent(), product);
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
        entity.setInsuranceDisbursement(input.creditLifeInsuranceMonthlyPercent()); entity.setInsuranceVehicle(input.vehicleInsuranceAnnualPercent());
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

    @Transactional
    public SimulationResource update(Integer id, SimulationRequest request) {
        UserEntity user = currentUserService.requireUser();
        var entity = accessible(id, user);
        requireClient(request.getClientId(), user);
        VehicleEntity vehicle = vehicleRepository.findByIdAndActiveTrue(request.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("VehÃ­culo no encontrado"));
        FinancialProductEntity product = requireProduct(request.getFinancialProductId());
        validateTeaRange(request.getTeaPercent(), product);
        validateInsuranceRanges(Boolean.TRUE.equals(request.getCreditLifeInsuranceEnabled()) ? request.getCreditLifeInsuranceMonthlyPercent() : null,
                Boolean.TRUE.equals(request.getVehicleInsuranceEnabled()) ? request.getVehicleInsuranceAnnualPercent() : null, product);
        var input = input(request.getVehiclePrice(), vehicle, request.getDownPaymentPercent(), request.getBalloonPercent(),
                request.getTeaPercent(), request.getCokTeaPercent(), request.getTermMonths(), request.getGraceType(), request.getGraceMonths(),
                request.getFirstPaymentDate(), request.getPaymentDay(), request.getCreditLifeInsuranceEnabled(), request.getCreditLifeInsuranceMonthlyPercent(),
                request.getVehicleInsuranceEnabled(), request.getVehicleInsuranceAnnualPercent(), product);
        SimulationResult result = engine.calculate(input);

        entity.setClientId(request.getClientId()); entity.setVehicleId(request.getVehicleId());
        entity.setEntityId(product.getFinancialInstitution().getId()); entity.setFinancialProduct(product);
        entity.setProductSnapshot(snapshot(product)); entity.setCurrency(product.getCurrency()); entity.setVehiclePrice(input.vehiclePrice());
        entity.setDownPaymentPercent(input.downPaymentPercent());
        entity.setDownPayment(input.vehiclePrice().multiply(input.downPaymentPercent()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));
        entity.setFinancedAmount(result.getFinancedAmount()); entity.setTerm(input.termMonths()); entity.setFirstPaymentDate(input.firstPaymentDate());
        entity.setDisbursementDate(input.firstPaymentDate().minusMonths(1)); entity.setPaymentDay(input.paymentDay());
        entity.setGraceType(input.graceType().name()); entity.setGraceMonths(input.graceMonths());
        entity.setBalloonEnabled(input.balloonPercent().signum() > 0); entity.setBalloonPercent(input.balloonPercent()); entity.setBalloonAmount(result.getBalloonAmount());
        entity.setInsuranceDisbursement(input.creditLifeInsuranceMonthlyPercent()); entity.setInsuranceVehicle(input.vehicleInsuranceAnnualPercent());
        entity.setTea(result.getTea()); entity.setTem(result.getTem()); entity.setCokTea(result.getCokTeaPercent()); entity.setCokTem(result.getCokTemPercent()); entity.setMonthlyPayment(result.getMonthlyPayment());
        entity.setVan(result.getVan()); entity.setTir(result.getTir()); entity.setTcea(result.getTcea());
        entity.setTotalInterest(result.getTotalInterest()); entity.setTotalInsurance(result.getTotalInsurance());
        entity.setTotalFees(result.getTotalCommissions()); entity.setTotalPayment(result.getTotalPayment()); entity.setStatus("Guardado");
        entity = simulationRepository.save(entity);

        scheduleRepository.deleteBySimulationId(entity.getId());
        Integer simulationId = entity.getId();
        var periods = result.getSchedule().stream().map(row -> period(simulationId, row)).toList();
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
        String code = p.getFinancialInstitution() == null ? null : p.getFinancialInstitution().getCode();
        if (!ENABLED_INSTITUTION_CODES.contains(code)) throw new UnprocessableEntityException("La entidad financiera no está habilitada para simulación");
        LocalDate today = LocalDate.now();
        if (p.getValidFrom().isAfter(today) || (p.getValidUntil()!=null && p.getValidUntil().isBefore(today))) throw new UnprocessableEntityException("El producto financiero no está vigente");
        return p;
    }
    private FinancialEngine.Input input(BigDecimal requestedPrice, VehicleEntity vehicle, BigDecimal down, BigDecimal balloon, BigDecimal tea, BigDecimal cokTea,
                                        int term, GraceType grace, int graceMonths, LocalDate first, int paymentDay,
                                        Boolean creditLifeEnabled, BigDecimal creditLifePercent,
                                        Boolean vehicleInsuranceEnabled, BigDecimal vehicleInsurancePercent,
                                        FinancialProductEntity product) {
        BigDecimal price = requestedPrice == null ? vehicle.getPrice() : requestedPrice;
        if (!product.getCurrency().equalsIgnoreCase(vehicle.getCurrency())) throw new UnprocessableEntityException("La moneda del vehículo no coincide con el producto");
        BigDecimal lifeRate = Boolean.TRUE.equals(creditLifeEnabled) ? (creditLifePercent == null ? defaultInsurancePercent(product, true) : creditLifePercent) : BigDecimal.ZERO;
        BigDecimal vehicleRate = Boolean.TRUE.equals(vehicleInsuranceEnabled) ? (vehicleInsurancePercent == null ? defaultInsurancePercent(product, false) : vehicleInsurancePercent) : BigDecimal.ZERO;
        return new FinancialEngine.Input(price, tea == null ? product.getTeaPercent() : tea, down, balloon, cokTea, term, grace, graceMonths, first, paymentDay, lifeRate, vehicleRate, product);
    }
    private void validateTeaRange(BigDecimal tea, FinancialProductEntity product) {
        if (tea == null) return;
        String code = product.getFinancialInstitution().getCode();
        if (!ENABLED_INSTITUTION_CODES.contains(code)) throw new UnprocessableEntityException("La entidad financiera no está habilitada para simulación");
        var range = teaRange(product);
        BigDecimal min = range[0];
        BigDecimal max = range[1];
        if (tea.compareTo(min) < 0 || tea.compareTo(max) > 0) {
            throw new UnprocessableEntityException("La TEA debe estar entre " + min + "% y " + max + "% para " + code);
        }
    }
    private void validateInsuranceRanges(BigDecimal creditLifeMonthlyPercent, BigDecimal vehicleAnnualPercent, FinancialProductEntity product) {
        if (creditLifeMonthlyPercent != null) {
            var range = insuranceRange(product, "DEGRAVAMEN", true);
            if (creditLifeMonthlyPercent.signum() < 0 || creditLifeMonthlyPercent.compareTo(range[0]) < 0 || creditLifeMonthlyPercent.compareTo(range[1]) > 0) {
                throw new UnprocessableEntityException("El seguro de desgravamen debe estar entre " + range[0] + "% y " + range[1] + "% mensual para " + product.getFinancialInstitution().getCode());
            }
        }
        if (vehicleAnnualPercent != null) {
            var range = insuranceRange(product, "VEHICULAR", false);
            if (vehicleAnnualPercent.signum() < 0 || vehicleAnnualPercent.compareTo(range[0]) < 0 || vehicleAnnualPercent.compareTo(range[1]) > 0) {
                throw new UnprocessableEntityException("El seguro vehicular debe estar entre " + range[0] + "% y " + range[1] + "% anual para " + product.getFinancialInstitution().getCode());
            }
        }
    }
    private BigDecimal[] insuranceRange(FinancialProductEntity product, String type, boolean monthly) {
        BigDecimal min = null;
        BigDecimal max = null;
        String insurancesJson = product.getFinancialInstitution().getInsurancesJson();
        try {
            JsonNode insurances = objectMapper.readTree(insurancesJson == null || insurancesJson.isBlank() ? "[]" : insurancesJson);
            for (JsonNode insurance : insurances) {
                if (!insurance.path("type").asText("").contains(type)) continue;
                BigDecimal fixed = decimal(insurance.path(monthly ? "ratePercentMonthly" : "ratePercentAnnual"));
                BigDecimal rateMin = decimal(insurance.path(monthly ? "ratePercentMonthlyMin" : "ratePercentAnnualMin"));
                BigDecimal rateMax = decimal(insurance.path(monthly ? "ratePercentMonthlyMax" : "ratePercentAnnualMax"));
                if (fixed != null) {
                    rateMin = fixed;
                    rateMax = fixed;
                }
                if (!monthly && fixed == null) {
                    BigDecimal monthlyFixed = decimal(insurance.path("ratePercentMonthly"));
                    if (monthlyFixed != null) {
                        rateMin = monthlyFixed.multiply(new BigDecimal("12"));
                        rateMax = rateMin;
                    }
                }
                min = min(min, rateMin);
                max = max(max, rateMax);
            }
        } catch (Exception ignored) {
            min = null;
            max = null;
        }
        Double institutionFallbackValue = monthly ? product.getFinancialInstitution().getInsuranceDisbursement() : product.getFinancialInstitution().getInsuranceVehicle();
        BigDecimal institutionFallback = institutionFallbackValue == null ? null : BigDecimal.valueOf(institutionFallbackValue);
        BigDecimal productFallback = monthly ? product.getCreditLifeInsuranceMonthlyPercent() : product.getVehicleInsuranceAnnualPercent();
        BigDecimal fallback = productFallback != null && productFallback.signum() > 0 ? productFallback : institutionFallback;
        min = min(min, fallback);
        max = max(max, fallback);
        return new BigDecimal[]{min, max};
    }
    private BigDecimal defaultInsurancePercent(FinancialProductEntity product, boolean monthly) {
        BigDecimal productValue = monthly ? product.getCreditLifeInsuranceMonthlyPercent() : product.getVehicleInsuranceAnnualPercent();
        if (productValue != null && productValue.signum() > 0) return productValue;
        Double institutionValue = monthly ? product.getFinancialInstitution().getInsuranceDisbursement() : product.getFinancialInstitution().getInsuranceVehicle();
        return institutionValue == null ? BigDecimal.ZERO : BigDecimal.valueOf(institutionValue);
    }
    private BigDecimal[] teaRange(FinancialProductEntity product) {
        var labelRange = parseRangeLabel(product.getFinancialInstitution().getTeaPublishedLabel());
        if (labelRange != null) return labelRange;

        BigDecimal min = null;
        BigDecimal max = null;
        String ratesJson = product.getFinancialInstitution().getRatesJson();
        try {
            JsonNode rates = objectMapper.readTree(ratesJson == null || ratesJson.isBlank() ? "[]" : ratesJson);
            for (JsonNode rate : rates) {
                if (!"TEA".equalsIgnoreCase(rate.path("rateType").asText("TEA"))) continue;
                String currency = rate.path("currency").asText(product.getCurrency());
                if (!product.getCurrency().equalsIgnoreCase(currency)) continue;
                BigDecimal rateMin = decimal(rate.path("minPercent"));
                BigDecimal rateMax = decimal(rate.path("maxPercent"));
                BigDecimal fixed = decimal(rate.path("fixedPercent"));
                if (fixed != null) {
                    rateMin = fixed;
                    rateMax = fixed;
                }
                min = min(min, rateMin);
                max = max(max, rateMax);
            }
        } catch (Exception ignored) {
            min = null;
            max = null;
        }
        if (max == null) max = product.getTeaPercent();
        if (min == null) min = BigDecimal.ZERO;
        return new BigDecimal[]{min, max};
    }
    private BigDecimal[] parseRangeLabel(String label) {
        if (label == null || label.isBlank()) return null;
        var matcher = RATE_NUMBER.matcher(label.replace(",", "."));
        List<BigDecimal> values = new ArrayList<>();
        while (matcher.find()) values.add(new BigDecimal(matcher.group()));
        if (values.isEmpty()) return null;
        if (values.size() == 1) return new BigDecimal[]{BigDecimal.ZERO, values.get(0)};
        return new BigDecimal[]{values.get(0), values.get(1)};
    }
    private BigDecimal decimal(JsonNode node) {
        return node == null || node.isMissingNode() || node.isNull() ? null : node.decimalValue();
    }
    private BigDecimal min(BigDecimal current, BigDecimal value) {
        if (value == null) return current;
        return current == null || value.compareTo(current) < 0 ? value : current;
    }
    private BigDecimal max(BigDecimal current, BigDecimal value) {
        if (value == null) return current;
        return current == null || value.compareTo(current) > 0 ? value : current;
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
        e.setInterest(r.getInterest()); e.setAmortization(r.getAmortization()); e.setInsurance(r.getInsurance());
        e.setCreditLifeInsurance(r.getCreditLifeInsurance()); e.setVehicleInsurance(r.getVehicleInsurance()); e.setCommission(r.getCommission());
        e.setTotalPayment(r.getTotalPayment()); e.setFinalFlow(r.getFinalFlow()); e.setBaseFlow(r.getBaseFlow());
        e.setFinalBalance(r.getFinalBalance()); e.setGraceType(r.getGraceType()); return e;
    }
    private PaymentRow row(PaymentScheduleEntity e) {
        var r=new PaymentRow(); r.setPeriod(e.getPeriod());r.setDate(e.getDate());r.setInitialBalance(e.getInitialBalance());r.setPayment(e.getPayment());
        r.setBalloonPayment(e.getBalloonPayment());r.setInterest(e.getInterest());r.setAmortization(e.getAmortization());r.setInsurance(e.getInsurance());
        r.setCreditLifeInsurance(e.getCreditLifeInsurance());r.setVehicleInsurance(e.getVehicleInsurance());
        r.setCommission(e.getCommission());r.setTotalPayment(e.getTotalPayment());r.setFinalFlow(e.getFinalFlow());r.setBaseFlow(e.getBaseFlow());
        r.setFinalBalance(e.getFinalBalance());r.setGraceType(e.getGraceType());return r;
    }
    private SimulationResource toResource(SimulationEntity e, List<PaymentRow> schedule) {
        var r=new SimulationResource(); r.setId(e.getId());r.setCode(e.getCode());r.setClientId(e.getClientId());r.setVehicleId(e.getVehicleId());
        r.setFinancialProductId(e.getFinancialProduct()==null?null:e.getFinancialProduct().getId());r.setCurrency(e.getCurrency());r.setVehiclePrice(e.getVehiclePrice());
        r.setDownPaymentPercent(e.getDownPaymentPercent());r.setFinancedAmount(e.getFinancedAmount());r.setTermMonths(e.getTerm());r.setFirstPaymentDate(e.getFirstPaymentDate());
        r.setPaymentDay(e.getPaymentDay());r.setGraceType(e.getGraceType());r.setGraceMonths(e.getGraceMonths());r.setBalloonPercent(e.getBalloonPercent());
        r.setCreditLifeInsuranceMonthlyPercent(e.getInsuranceDisbursement());r.setCreditLifeInsuranceEnabled(e.getInsuranceDisbursement()!=null&&e.getInsuranceDisbursement().signum()>0);
        r.setVehicleInsuranceAnnualPercent(e.getInsuranceVehicle());r.setVehicleInsuranceEnabled(e.getInsuranceVehicle()!=null&&e.getInsuranceVehicle().signum()>0);
        r.setMonthlyPayment(e.getMonthlyPayment());r.setTeaPercent(e.getTea());r.setTemPercent(e.getTem());r.setCokTeaPercent(e.getCokTea());r.setCokTemPercent(e.getCokTem());r.setTirPercent(e.getTir());r.setTceaPercent(e.getTcea());r.setVan(e.getVan());
        r.setTotalInterest(e.getTotalInterest());r.setTotalInsurance(e.getTotalInsurance());r.setTotalFees(e.getTotalFees());r.setTotalPayment(e.getTotalPayment());
        r.setProductSnapshot(e.getProductSnapshot());r.setCreatedAt(e.getCreatedAtTimestamp());r.setSchedule(schedule);return r;
    }
}
