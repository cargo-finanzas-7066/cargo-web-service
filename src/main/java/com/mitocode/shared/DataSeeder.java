package com.mitocode.shared;

import com.mitocode.customers.persistence.entities.CustomerEntity;
import com.mitocode.customers.persistence.repositories.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;
import com.mitocode.iam.persistence.repositories.UserRepository;
import org.springframework.core.annotation.Order;
import java.math.BigDecimal;

@Component
@Profile("dev")
@Order(30)
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    @Override
    public void run(String... args) {
        seedCustomers();
    }

    private void seedCustomers() {
        if (customerRepository.count() > 0) {
            return;
        }

        var owner = userRepository.findAll().stream().filter(u -> Boolean.TRUE.equals(u.getActive())).findFirst().orElse(null);
        if (owner == null) return;
        customerRepository.save(customer("DNI", "45829103", "Carlos Eduardo", "Mendez", "c.mendez@outlook.com", "+51 982 341 092", owner));
        customerRepository.save(customer("DNI", "72193845", "Ana Lucia", "Torres", "analtorres@gmail.com", "+51 912 003 841", owner));
        customerRepository.save(customer("CE", "09384721", "Roberto Franco", "Valdivia", "r.franco@empresa.pe", "+51 945 281 332", owner));
    }

    private CustomerEntity customer(String docType, String docNumber, String names, String surnames, String email, String phone,
                                    com.mitocode.iam.persistence.entities.UserEntity owner) {
        var customer = new CustomerEntity();
        customer.setDocType(docType);
        customer.setOwner(owner);
        customer.setDocNumber(docNumber);
        customer.setNames(names);
        customer.setSurnames(surnames);
        customer.setEmail(email);
        customer.setPhone(phone);
        customer.setMonthlyIncome(new BigDecimal("6500.00"));
        customer.setOccupation("Dependiente");
        customer.setStatus("Activo");
        return customer;
    }

}
