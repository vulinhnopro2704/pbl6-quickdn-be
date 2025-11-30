package com.pbl6.order.service;

import com.pbl6.order.dto.DistanceMatrixRequest;
import com.pbl6.order.dto.DistanceMatrixResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Service
public class GooongMapClientService {

  private final WebClient webClient;
  private final String distanceMatrixPath;

  public GooongMapClientService(
      WebClient gooongMapWebClient,
      @Value("${gooongmap.distance-matrix-path}") String distanceMatrixPath) {
    this.webClient = gooongMapWebClient;
    this.distanceMatrixPath = distanceMatrixPath;
  }

  public Mono<DistanceMatrixResponse> getDistanceMatrixWithQuery(DistanceMatrixRequest req) {
    return webClient
        .get()
        .uri(
            uriBuilder ->
                uriBuilder
                    .path(distanceMatrixPath) // e.g. "/distance-matrix"
                    .queryParam("origins", req.origins())
                    .queryParam("destinations", req.destinations())
                    .queryParam("vehicle", req.vehicle())
                    .build())
        .retrieve()
        .bodyToMono(DistanceMatrixResponse.class)
        .timeout(Duration.ofSeconds(5))
        .onErrorResume(e -> Mono.just(new DistanceMatrixResponse(List.of())));
  }
}
