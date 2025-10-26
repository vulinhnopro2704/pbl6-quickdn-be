package com.pbl6.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class AuthServiceApplication {

	private static final Logger logger = LoggerFactory.getLogger(AuthServiceApplication.class);

	public static void main(String[] args) {
		// Log environment variables related to Redis
		logger.info("========== REDIS ENVIRONMENT VARIABLES ==========");
		logger.info("REDIS_HOST: {}", System.getenv("REDIS_HOST"));
		logger.info("REDIS_PORT: {}", System.getenv("REDIS_PORT"));
		logger.info("System Property - spring.data.redis.host: {}", System.getProperty("spring.data.redis.host"));
		logger.info("System Property - spring.data.redis.port: {}", System.getProperty("spring.data.redis.port"));
		logger.info("================================================");

		ApplicationContext ctx = SpringApplication.run(AuthServiceApplication.class, args);
		
		// Log the actual Redis configuration after application starts
		logger.info("========== SPRING REDIS CONFIGURATION ==========");
		try {
			String redisHost = ctx.getEnvironment().getProperty("spring.data.redis.host");
			String redisPort = ctx.getEnvironment().getProperty("spring.data.redis.port");
			logger.info("Spring Redis Host: {}", redisHost);
			logger.info("Spring Redis Port: {}", redisPort);
		} catch (Exception e) {
			logger.error("Error reading Redis configuration", e);
		}
		logger.info("================================================");
	}

}
