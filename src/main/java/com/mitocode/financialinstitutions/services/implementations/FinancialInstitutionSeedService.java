package com.mitocode.financialinstitutions.services.implementations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mitocode.financialinstitutions.persistence.entities.FinancialInstitutionEntity;
import com.mitocode.financialinstitutions.persistence.repositories.FinancialInstitutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import java.util.Set;

@Component
@Profile("dev")
@Order(10)
@RequiredArgsConstructor
public class FinancialInstitutionSeedService implements CommandLineRunner {
    private static final Set<String> ENABLED_CODES = Set.of("BCP", "BBVA", "INTERBANK");
    private final FinancialInstitutionRepository financialInstitutionRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void run(String... args) throws Exception {
        var resource = new ClassPathResource("jsonData/financial_entities_seed.json");
        JsonNode root = objectMapper.readTree(resource.getInputStream());
        String reviewedAt = root.path("lastReviewedAt").asText("");

        for (JsonNode node : root.path("entities")) {
            String code = node.path("code").asText();
            if (!ENABLED_CODES.contains(code)) continue;
            var entity = financialInstitutionRepository.findByCode(code).orElse(new FinancialInstitutionEntity());
            entity.setCode(code);
            entity.setDisplayOrder(node.path("displayOrder").asInt());
            entity.setName(node.path("name").asText());
            entity.setShortName(node.path("shortName").asText());
            entity.setType(node.path("type").asText());
            entity.setLogoText(node.path("logoText").asText());
            entity.setCurrency(node.path("currency").asText("PEN"));
            entity.setCreditType(node.path("creditType").asText());
            entity.setProduct(node.path("productName").asText());
            entity.setCanUseInSimulation(node.path("canUseInSimulation").asBoolean(true));
            entity.setStatus(node.path("isVisible").asBoolean(true) ? "Activo" : "Inactivo");

            JsonNode summary = node.path("summary");
            entity.setTeaPublishedLabel(summary.path("teaPublishedLabel").asText("Referencial"));
            entity.setMinimumInitialLabel(summary.path("minimumInitialLabel").asText("No publicado"));
            entity.setMaximumFinancingLabel(summary.path("maximumFinancingLabel").asText("No publicado"));
            entity.setTermLabel(summary.path("termLabel").asText("No publicado"));
            entity.setGraceLabel(summary.path("graceLabel").asText("No publicado"));
            entity.setInsuranceSummaryLabel(summary.path("insuranceSummaryLabel").asText("No publicado"));
            entity.setChargesSummaryLabel(summary.path("chargesSummaryLabel").asText("No publicado"));

            JsonNode conditions = node.path("conditions");
            entity.setMinDownPayment(nullableDouble(conditions.path("minimumDownPaymentPercent")));
            entity.setMaxFinancing(nullableDouble(conditions.path("maximumFinancingPercent")));
            entity.setMinTerm(nullableInt(conditions.path("minTermMonths")));
            entity.setMaxTerm(nullableInt(conditions.path("maxTermMonths")));

            JsonNode firstRate = node.path("rates").isArray() && node.path("rates").size() > 0 ? node.path("rates").get(0) : null;
            if (firstRate != null) {
                entity.setTea(firstNonNullDouble(firstRate.path("fixedPercent"), firstRate.path("minPercent"), firstRate.path("maxPercent")));
            }

            entity.setInsuranceDisbursement(findInsuranceMonthly(node, "DEGRAVAMEN"));
            entity.setInsuranceVehicle(findVehicleInsurance(node));
            // El Excel no incluye portes. El envío físico de estado de cuenta es opcional
            // y no debe convertirse en un cargo mensual del producto.
            entity.setMonthlyFee(0.0);
            entity.setAdminCost(0.0);
            entity.setRatesJson(objectMapper.writeValueAsString(node.path("rates")));
            entity.setInsurancesJson(objectMapper.writeValueAsString(node.path("insurances")));
            entity.setChargesJson(objectMapper.writeValueAsString(node.path("charges")));

            JsonNode verification = node.path("verification");
            entity.setSourceName(verification.path("sourceName").asText("Fuente oficial"));
            entity.setSourceDate(reviewedAt);
            entity.setVerificationStatus(verification.path("status").asText("VERIFIED"));
            applyExcelFirstSheetCorrections(entity);
            financialInstitutionRepository.save(entity);
        }
        financialInstitutionRepository.findAll().stream()
                .filter(entity -> entity.getCode() != null && !ENABLED_CODES.contains(entity.getCode()))
                .forEach(entity -> {
                    entity.setCanUseInSimulation(false);
                    entity.setStatus("Inactivo");
                    financialInstitutionRepository.save(entity);
                });
    }

    private void applyExcelFirstSheetCorrections(FinancialInstitutionEntity entity) {
        if ("BCP".equals(entity.getCode())) {
            entity.setTeaPublishedLabel("8.00% - 20.26%");
            entity.setMinimumInitialLabel("Desde 0%");
            entity.setMaximumFinancingLabel("Hasta 100%");
            entity.setMinDownPayment(0.0);
            entity.setMaxFinancing(100.0);
            entity.setMinTerm(3);
            entity.setMaxTerm(60);
            entity.setTea(12.50);
            entity.setCanUseInSimulation(true);
            entity.setVerificationStatus("VERIFIED_FROM_DATASET");
        }
        if ("BBVA".equals(entity.getCode())) {
            entity.setTeaPublishedLabel("1.99% - 24.99%");
            entity.setMinimumInitialLabel("Desde 0%");
            entity.setMaximumFinancingLabel("Hasta 100%");
            entity.setMinDownPayment(0.0);
            entity.setMaxFinancing(100.0);
            entity.setMinTerm(12);
            entity.setMaxTerm(72);
            entity.setTea(8.65);
            entity.setCanUseInSimulation(true);
            entity.setVerificationStatus("VERIFIED_FROM_DATASET");
        }
        if ("INTERBANK".equals(entity.getCode())) {
            entity.setTeaPublishedLabel("Hasta 16.39%");
            entity.setMinimumInitialLabel("Desde 0%");
            entity.setMaximumFinancingLabel("Hasta 100%");
            entity.setMinDownPayment(0.0);
            entity.setMaxFinancing(100.0);
            entity.setMinTerm(12);
            entity.setMaxTerm(60);
            entity.setTea(14.49);
            entity.setCanUseInSimulation(true);
            entity.setVerificationStatus("VERIFIED_FROM_DATASET");
        }
    }

    private Double nullableDouble(JsonNode node) {
        return node == null || node.isMissingNode() || node.isNull() ? null : node.asDouble();
    }

    private Integer nullableInt(JsonNode node) {
        return node == null || node.isMissingNode() || node.isNull() ? null : node.asInt();
    }

    private Double firstNonNullDouble(JsonNode... nodes) {
        for (JsonNode node : nodes) {
            if (node != null && !node.isMissingNode() && !node.isNull()) {
                return node.asDouble();
            }
        }
        return 0.0;
    }

    private Double findInsuranceMonthly(JsonNode entity, String type) {
        for (JsonNode insurance : entity.path("insurances")) {
            if (insurance.path("type").asText().contains(type)) {
                return firstNonNullDouble(insurance.path("ratePercentMonthly"), insurance.path("ratePercentAnnual"));
            }
        }
        return 0.0;
    }

    private Double findVehicleInsurance(JsonNode entity) {
        for (JsonNode insurance : entity.path("insurances")) {
            if (insurance.path("type").asText().contains("VEHICULAR")) {
                Double annualRate = nullableDouble(insurance.path("ratePercentAnnual"));
                if (annualRate != null) {
                    return annualRate;
                }
                Double annualMinRate = nullableDouble(insurance.path("ratePercentAnnualMin"));
                if (annualMinRate != null) {
                    return annualMinRate;
                }
                Double monthlyRate = nullableDouble(insurance.path("ratePercentMonthly"));
                if (monthlyRate != null) {
                    return monthlyRate * 12.0;
                }
            }
        }
        return 0.0;
    }

    private Double findMonthlyCharge(JsonNode entity) {
        for (JsonNode charge : entity.path("charges")) {
            if (charge.path("type").asText().equals("PHYSICAL_STATEMENT_DELIVERY") && !charge.path("amount").isNull()) {
                return charge.path("amount").asDouble();
            }
        }
        return 0.0;
    }
}
