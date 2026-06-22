package com.mitocode.shared;

import com.mitocode.customers.persistence.entities.CustomerEntity;
import com.mitocode.customers.persistence.repositories.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {
    private final CustomerRepository customerRepository;

    @Override
    public void run(String... args) {
        seedCustomers();
    }

    private void seedCustomers() {
        if (customerRepository.count() > 0) {
            return;
        }

        customerRepository.save(customer("DNI", "45829103", "Carlos Eduardo", "Mendez", "c.mendez@outlook.com", "+51 982 341 092"));
        customerRepository.save(customer("DNI", "72193845", "Ana Lucia", "Torres", "analtorres@gmail.com", "+51 912 003 841"));
        customerRepository.save(customer("CE", "09384721", "Roberto Franco", "Valdivia", "r.franco@empresa.pe", "+51 945 281 332"));
    }

    private CustomerEntity customer(String docType, String docNumber, String names, String surnames, String email, String phone) {
        var customer = new CustomerEntity();
        customer.setDocType(docType);
        customer.setDocNumber(docNumber);
        customer.setNames(names);
        customer.setSurnames(surnames);
        customer.setEmail(email);
        customer.setPhone(phone);
        customer.setMonthlyIncome(6500.0);
        customer.setOccupation("Dependiente");
        customer.setStatus("Activo");
        return customer;
    }

}
