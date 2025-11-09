package com.pbl6.payment.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI paymentServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Payment Service API - payOS Integration")
                        .description("""
                                ## 🎯 Overview
                                
                                RESTful API for managing payments through payOS gateway integration.
                                
                                ## 🔑 Key Features
                                
                                - ✅ Create payment links with payOS
                                - ✅ Retrieve payment status
                                - ✅ Webhook handling for payment notifications
                                - ✅ HMAC_SHA256 signature verification
                                - ✅ Idempotency by orderCode
                                - ✅ PostgreSQL persistence
                                
                                ## 🚀 Getting Started
                                
                                1. Configure environment variables (see README.md)
                                2. Create payment using POST /api/payments
                                3. Redirect user to `checkoutUrl` in response
                                4. Receive webhook notification when payment completes
                                
                                ## 📚 Documentation
                                
                                - payOS Docs: https://payos.vn/docs/api/
                                - GitHub: https://github.com/vulinhnopro2704/pbl6-quickdn-be
                                
                                ## ⚠️ Important Notes
                                
                                - All amounts are in VND (Vietnamese Dong)
                                - orderCode must be unique per payment
                                - Payments are idempotent - duplicate orderCode returns existing payment
                                - Signatures are verified using HMAC_SHA256
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("PBL6 Team")
                                .email("support@quickdn.com")
                                .url("https://github.com/vulinhnopro2704"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development Server"),
                        new Server()
                                .url("https://api.quickdn.com")
                                .description("Production Server")))
                .tags(List.of(
                        new Tag()
                                .name("Payment")
                                .description("Payment creation and management endpoints"),
                        new Tag()
                                .name("Webhook")
                                .description("Webhook endpoints for payOS notifications")));
    }
}
