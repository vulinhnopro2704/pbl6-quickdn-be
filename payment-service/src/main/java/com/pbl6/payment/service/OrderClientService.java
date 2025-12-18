package com.pbl6.payment.service;

import com.pbl6.payment.dto.order.PaymentSuccessRequest;
import com.pbl6.payment.exception.AppException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class OrderClientService {

  private final WebClient webClient;
  private final String paymentSuccessPath;

  public OrderClientService(
      @Qualifier("orderWebClient") WebClient webClient,
      @Value("${order.internal.payment-success-path}") String paymentSuccessPath) {
    this.webClient = webClient;
    this.paymentSuccessPath = paymentSuccessPath;
  }

  public Mono<?> updateOrderAndFindShipper(PaymentSuccessRequest request) {
    return webClient
        .post()
        .uri(paymentSuccessPath)
        .bodyValue(request)
        .retrieve()
        .onStatus(
            HttpStatusCode::isError,
            response ->
                response
                    .bodyToMono(String.class)
                    .flatMap(
                        body ->
                            Mono.error(
                                AppException.internal("Notify order service failed: " + body))))
        .bodyToMono(Void.class)
        .timeout(Duration.ofSeconds(5));
  }
}
