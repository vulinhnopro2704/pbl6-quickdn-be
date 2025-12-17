package com.pbl6.order.service;

import com.pbl6.order.dto.payment.CreatePaymentRequest;
import com.pbl6.order.dto.payment.PaymentResponse;
import com.pbl6.order.entity.PaymentStatus;
import com.pbl6.order.exception.AppException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class PaymentClientService {

  private final WebClient webClient;
  private final String createPaymentPath;
  private final String queryPaymentPath;

  public PaymentClientService(
      @Qualifier("paymentWebClient") WebClient webClient,
      @Value("${payment.create-payment-path}") String createPaymentPath,
      @Value("${payment.query-payment-path}") String queryPaymentPath) {
    this.webClient = webClient;
    this.createPaymentPath = createPaymentPath;
    this.queryPaymentPath = queryPaymentPath;
  }

  public Mono<PaymentResponse> createPayment(CreatePaymentRequest request) {
    return webClient
        .post()
        .uri(createPaymentPath)
        .bodyValue(request)
        .retrieve()
        .onStatus(
            HttpStatusCode::isError,
            response ->
                response
                    .bodyToMono(String.class)
                    .flatMap(
                        body ->
                            Mono.error(AppException.internal("Create payment failed: " + body))))
        .bodyToMono(PaymentResponse.class)
        .timeout(Duration.ofSeconds(5));
  }

  public Mono<PaymentStatus> getPaymentStatus(String paymentId) {
    return webClient
        .get()
        .uri(uriBuilder -> uriBuilder.path(queryPaymentPath).build(paymentId))
        .retrieve()
        .bodyToMono(PaymentStatus.class)
        .timeout(Duration.ofSeconds(5));
  }
}
