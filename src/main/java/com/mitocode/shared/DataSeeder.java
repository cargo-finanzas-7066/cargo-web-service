package com.mitocode.shared;

import com.mitocode.entity.ClientEntity;
import com.mitocode.entity.FinancialEntityEntity;
import com.mitocode.repository.ClientRepository;
import com.mitocode.repository.FinancialEntityRepository;
import com.mitocode.vehicles.persistence.entities.VehicleEntity;
import com.mitocode.vehicles.persistence.repositories.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {
    private final VehicleRepository vehicleRepository;
    private final ClientRepository clientRepository;
    private final FinancialEntityRepository financialEntityRepository;

    @Override
    public void run(String... args) {
        seedVehicles();
        seedClients();
        seedFinancialEntities();
    }

    private void seedVehicles() {
        if (vehicleRepository.count() > 0) {
            return;
        }

        vehicleRepository.save(vehicle("Toyota", "Corolla Premium", 2024, "Sedán", 85000.0, "Mitsui Automotriz", "https://images.unsplash.com/photo-1623869675781-80aa31012a5a?auto=format&fit=crop&w=900&q=80"));
        vehicleRepository.save(vehicle("Hyundai", "Elantra GLS", 2025, "Sedán", 78500.0, "Gildemeister Retail", "https://images.unsplash.com/photo-1606152421802-db97b9c7a11b?auto=format&fit=crop&w=900&q=80"));
        vehicleRepository.save(vehicle("Kia", "Sportage LX", 2024, "SUV", 115200.0, "Limautos", "https://images.unsplash.com/photo-1609521263047-f8f205293f24?auto=format&fit=crop&w=900&q=80"));
        vehicleRepository.save(vehicle("Volkswagen", "Tiguan Allspace", 2024, "SUV", 142900.0, "Maquinarias S.A.", "https://images.unsplash.com/photo-1549925862-990a1e87733f?auto=format&fit=crop&w=900&q=80"));
        vehicleRepository.save(vehicle("Nissan", "Sentra Exclusive", 2024, "Sedán", 94600.0, "Limautos", "https://images.unsplash.com/photo-1617469767053-d3b523a0b982?auto=format&fit=crop&w=900&q=80"));
        vehicleRepository.save(vehicle("Mazda", "CX-5 Core", 2024, "SUV", 128400.0, "Limautos", "https://images.unsplash.com/photo-1619767886558-efdc259cde1a?auto=format&fit=crop&w=900&q=80"));
    }

    private void seedClients() {
        if (clientRepository.count() > 0) {
            return;
        }

        clientRepository.save(client("Ricardo", "Palma", "ricardo.palma@correo.pe"));
        clientRepository.save(client("Elena", "García", "elena.garcia@correo.pe"));
        clientRepository.save(client("Juan", "Pérez", "juan.perez@correo.pe"));
        clientRepository.save(client("Carmen", "Rosa", "carmen.rosa@correo.pe"));
    }

    private void seedFinancialEntities() {
        if (financialEntityRepository.count() > 0) {
            return;
        }

        financialEntityRepository.save(financialEntity("BCP", 12.5));
        financialEntityRepository.save(financialEntity("Interbank", 11.8));
        financialEntityRepository.save(financialEntity("BBVA", 13.2));
        financialEntityRepository.save(financialEntity("Scotiabank", 14.0));
    }

    private VehicleEntity vehicle(String brand, String model, Integer year, String category, Double price, String dealer, String imageUrl) {
        var vehicle = new VehicleEntity();
        vehicle.setBrand(brand);
        vehicle.setModel(model);
        vehicle.setYear(year);
        vehicle.setCategory(category);
        vehicle.setPrice(price);
        vehicle.setCurrency("PEN");
        vehicle.setDealer(dealer);
        vehicle.setImageUrl(imageUrl);
        vehicle.setStatus("Disponible");
        return vehicle;
    }

    private ClientEntity client(String names, String surnames, String email) {
        var client = new ClientEntity();
        client.setDocType("DNI");
        client.setDocNumber(String.valueOf(70000000 + Math.abs(email.hashCode() % 999999)));
        client.setNames(names);
        client.setSurnames(surnames);
        client.setEmail(email);
        client.setPhone("999888777");
        client.setMonthlyIncome(6500.0);
        client.setOccupation("Dependiente");
        client.setStatus("Activo");
        return client;
    }

    private FinancialEntityEntity financialEntity(String name, Double tea) {
        var entity = new FinancialEntityEntity();
        entity.setName(name);
        entity.setType("Banco");
        entity.setProduct("Crédito vehicular");
        entity.setTea(tea);
        entity.setStatus("Activo");
        return entity;
    }
}
