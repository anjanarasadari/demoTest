package com.payable.demotest.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI Configuration for Order Management System.
 * Provides API documentation and metadata for the REST endpoints.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Order Management System API")
                        .description("Scalable order management system with comprehensive REST API for managing orders, payments, inventory, and shipments.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("API Support")
                                .email("support@ordermanagement.com")
                                .url("https://ordermanagement.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development Server"),
                        new Server()
                                .url("https://api.ordermanagement.com")
                                .description("Production Server")));
    }
}

