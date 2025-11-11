package com.pbl6.goongmap.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI goongmapServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Goongmap Service API")
                        .description("Goong Map Integration Service for PBL6 QuickDN Backend - Provides location services, geocoding, and route planning")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("PBL6 Team")
                                .email("support@quickdn.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server().url("http://localhost:8082/api/goongmap").description("Local Development Server"),
                        new Server().url("http://localhost:8080/api/goongmap").description("Local Gateway Server"),
                        new Server().url("https://quickdn.undo.it/api/goongmap").description("Production Server")));
    }
}
