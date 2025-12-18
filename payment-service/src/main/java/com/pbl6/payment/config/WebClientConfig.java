package com.pbl6.payment.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

@Configuration
public class WebClientConfig {

  @Bean(name = "orderWebClient")
  public WebClient orderWebClient(
      @Value("${order.base-url}") String baseUrl,
      @Value("${internal.order.service-token}") String serviceToken,
      @Value("${webclient.connect-timeout-ms}") int connectTimeoutMs,
      @Value("${webclient.read-timeout-ms}") int readTimeoutMs,
      @Value("${webclient.max-in-memory-size-bytes}") int maxInMemorySize) {
    return buildWebClient(baseUrl, serviceToken, connectTimeoutMs, readTimeoutMs, maxInMemorySize);
  }

  private WebClient buildWebClient(
      String baseUrl,
      String serviceToken,
      int connectTimeoutMs,
      int readTimeoutMs,
      int maxInMemorySize) {

    TcpClient tcpClient =
        TcpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMs)
            .doOnConnected(
                conn -> conn.addHandlerLast(new ReadTimeoutHandler(readTimeoutMs / 1000)));

    HttpClient httpClient = HttpClient.from(tcpClient);

    ExchangeStrategies strategies =
        ExchangeStrategies.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(maxInMemorySize))
            .build();

    return WebClient.builder()
        .baseUrl(baseUrl)
        .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + serviceToken)
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .exchangeStrategies(strategies)
        .build();
  }
}
