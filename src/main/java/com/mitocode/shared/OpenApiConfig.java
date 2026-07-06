package com.mitocode.shared;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.*;
import org.springframework.context.annotation.*;

@Configuration
public class OpenApiConfig {
    @Bean OpenAPI cargoOpenApi(){
        return new OpenAPI().info(new Info().title("CarGo Financial API").version("v1")
                        .description("API REST para catálogos, clientes, cotizaciones y simulaciones de crédito vehicular."))
                .components(new Components().addSecuritySchemes("bearerAuth",new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
