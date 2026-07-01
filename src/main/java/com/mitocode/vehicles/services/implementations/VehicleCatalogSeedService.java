package com.mitocode.vehicles.services.implementations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mitocode.vehicles.persistence.entities.VehicleEntity;
import com.mitocode.vehicles.persistence.repositories.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class VehicleCatalogSeedService implements CommandLineRunner {
    private final VehicleRepository vehicleRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void run(String... args) throws Exception {
        var resource = new ClassPathResource("jsonData/vehicle_catalog_seed.json");
        if (!resource.exists()) {
            return;
        }

        JsonNode root = objectMapper.readTree(resource.getInputStream());
        int order = 0;
        for (JsonNode node : root.path("vehicles")) {
            String code = node.path("code").asText();
            if (code.isBlank()) {
                continue;
            }
            var vehicle = vehicleRepository.findByCode(code).orElse(new VehicleEntity());
            vehicle.setCode(code);
            vehicle.setBrand(clean(node.path("brand").asText()));
            vehicle.setModel(clean(node.path("model").asText()));
            vehicle.setYear(node.path("year").asInt(2026));
            vehicle.setCategory(clean(node.path("category").asText("Referencia")));
            vehicle.setPrice(node.path("referencePrice").path("amount").decimalValue());
            vehicle.setCurrency(node.path("referencePrice").path("currency").asText("PEN"));
            vehicle.setDealer(clean(node.path("dealer").asText()));
            vehicle.setDescription(clean(node.path("status").path("message").asText()));
            vehicle.setImageUrl(node.path("ui").path("imageUrl").isNull() ? null : node.path("ui").path("imageUrl").asText());
            vehicle.setStatus(node.path("simulation").path("canUseInSimulation").asBoolean(true) ? "Disponible" : "No disponible");
            vehicleRepository.save(vehicle);
            order++;
            if (order >= 120) {
                break;
            }
        }
    }

    private String clean(String value) {
        return value == null ? null : value
                .replace("Ã¡", "á").replace("Ã©", "é").replace("Ã­", "í").replace("Ã³", "ó").replace("Ãº", "ú")
                .replace("Ã±", "ñ").replace("PerÃº", "Perú").replace("SedÃ¡n", "Sedán");
    }
}
