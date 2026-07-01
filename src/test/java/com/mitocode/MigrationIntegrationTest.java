package com.mitocode;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.http.MediaType;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@AutoConfigureMockMvc
class MigrationIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Container static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");
    @DynamicPropertySource static void properties(DynamicPropertyRegistry r){
        r.add("spring.datasource.url",POSTGRES::getJdbcUrl);r.add("spring.datasource.username",POSTGRES::getUsername);r.add("spring.datasource.password",POSTGRES::getPassword);
        r.add("jwt.secret",()->"test-secret-key-with-at-least-32-characters");
        r.add("app.bootstrap.admin-email",()->"admin@cargo.test");
        r.add("app.bootstrap.admin-password",()->"AdminTest123!");
    }
    @Test void frontendSecuritySmokeFlow() throws Exception {
        mvc.perform(get("/api/v1/vehicles")).andExpect(status().isOk());
        mvc.perform(get("/api/v1/clients")).andExpect(status().isUnauthorized());

        String signUp=mvc.perform(post("/api/v1/auth/sign-up").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Asesor Test\",\"email\":\"advisor@cargo.test\",\"password\":\"Advisor123!\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String advisorToken=json.readTree(signUp).path("token").asText();
        mvc.perform(post("/api/v1/vehicles").header("Authorization","Bearer "+advisorToken).contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"TEST-1\",\"brand\":\"Test\",\"model\":\"Car\",\"year\":2026,\"price\":30000,\"currency\":\"PEN\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/clients").header("Authorization","Bearer "+advisorToken).contentType(MediaType.APPLICATION_JSON)
                .content("{\"docType\":\"DNI\",\"docNumber\":\"12345678\",\"names\":\"Cliente\",\"surnames\":\"Test\",\"monthlyIncome\":5000}"))
                .andExpect(status().isCreated());

        String admin=mvc.perform(post("/api/v1/auth/sign-in").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"admin@cargo.test\",\"password\":\"AdminTest123!\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String adminToken=json.readTree(admin).path("token").asText();
        mvc.perform(post("/api/v1/vehicles").header("Authorization","Bearer "+adminToken).contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"TEST-1\",\"brand\":\"Test\",\"model\":\"Car\",\"year\":2026,\"price\":30000,\"currency\":\"PEN\"}"))
                .andExpect(status().isCreated());
    }
}
