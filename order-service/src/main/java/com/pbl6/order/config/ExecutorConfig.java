package com.pbl6.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ExecutorConfig {

  @Bean(destroyMethod = "shutdown")
  public ExecutorService pushExecutor() {
    // Tùy chỉnh: bắt đầu với fixed pool 20. Tune theo load thực tế.
    return Executors.newFixedThreadPool(20);
  }
}
