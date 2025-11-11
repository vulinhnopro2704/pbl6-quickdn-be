package com.pbl6.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ApiGatewayApplication {

	private static final Logger logger = LoggerFactory.getLogger(ApiGatewayApplication.class);

	@Value("${services.auth.url:http://localhost:8081}")
	private String authServiceUrl;

	@Value("${services.goongmap.url:http://localhost:8082}")
	private String goongmapServiceUrl;

	@Value("${services.order.url:http://localhost:8083}")
	private String orderServiceUrl;

	@Value("${services.payment.url:http://localhost:8084}")
	private String paymentServiceUrl;

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}

	@Bean
	public CommandLineRunner logServiceUrls() {
		return args -> {
			logger.info("Resolved service URLs at startup:");
			logger.info("AUTH_SERVICE_URL={}", authServiceUrl);
			logger.info("GOONGMAP_SERVICE_URL={}", goongmapServiceUrl);
			logger.info("ORDER_SERVICE_URL={}", orderServiceUrl);
			logger.info("PAYMENT_SERVICE_URL={}", paymentServiceUrl);
		};
	}

}
