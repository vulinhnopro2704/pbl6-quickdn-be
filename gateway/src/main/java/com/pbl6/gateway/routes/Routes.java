package com.pbl6.gateway.routes;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.*;

@Configuration
public class Routes {

    @Value("${services.auth.url}")
    private String authServiceUrl;

    @Value("${services.goongmap.url}")
    private String goongmapServiceUrl;

    @Value("${services.order.url}")
    private String orderServiceUrl;

    @Value("${services.payment.url}")
    private String paymentServiceUrl;

    @Bean
    public RouterFunction<ServerResponse> authServiceRoute() {
        // Use leading slashes on paths and the configured service URL (works inside Docker network)
        return GatewayRouterFunctions.route("auth-service")
                .route(RequestPredicates.path("/api/auth/test"), HandlerFunctions.http(authServiceUrl))
                .route(RequestPredicates.path("/api/auth/register"), HandlerFunctions.http(authServiceUrl))
                .route(RequestPredicates.path("/api/auth/login"), HandlerFunctions.http(authServiceUrl))
                .route(RequestPredicates.path("/api/auth/logout"), HandlerFunctions.http(authServiceUrl))
                // Swagger/OpenAPI endpoints for auth-service
                .route(RequestPredicates.path("/api/auth/v3/api-docs/**"), HandlerFunctions.http(authServiceUrl))
                .route(RequestPredicates.path("/api/auth/swagger-ui/**"), HandlerFunctions.http(authServiceUrl))
                .route(RequestPredicates.path("/api/auth/swagger-ui.html"), HandlerFunctions.http(authServiceUrl))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> goongMapServiceRoute() {
        return GatewayRouterFunctions.route("goongmap-service")
                .route(RequestPredicates.path("/api/goongmap/test"), HandlerFunctions.http(goongmapServiceUrl))
                // Swagger/OpenAPI endpoints for goongmap-service
                .route(RequestPredicates.path("/api/goongmap/v3/api-docs/**"), HandlerFunctions.http(goongmapServiceUrl))
                .route(RequestPredicates.path("/api/goongmap/swagger-ui/**"), HandlerFunctions.http(goongmapServiceUrl))
                .route(RequestPredicates.path("/api/goongmap/swagger-ui.html"), HandlerFunctions.http(goongmapServiceUrl))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> orderServiceRoute() {
        return GatewayRouterFunctions.route("order-service")
                .route(RequestPredicates.path("/api/order/test"), HandlerFunctions.http(orderServiceUrl))
                .route(RequestPredicates.path("/api/order"), HandlerFunctions.http(orderServiceUrl))
                // Swagger/OpenAPI endpoints for order-service
                .route(RequestPredicates.path("/api/order/v3/api-docs/**"), HandlerFunctions.http(orderServiceUrl))
                .route(RequestPredicates.path("/api/order/swagger-ui/**"), HandlerFunctions.http(orderServiceUrl))
                .route(RequestPredicates.path("/api/order/swagger-ui.html"), HandlerFunctions.http(orderServiceUrl))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> paymentServiceRoute() {
        return GatewayRouterFunctions.route("payment-service")
                .route(RequestPredicates.path("/api/payment/**"), HandlerFunctions.http(paymentServiceUrl))
                // Swagger/OpenAPI endpoints for payment-service
                .route(RequestPredicates.path("/api/payment/v3/api-docs/**"), HandlerFunctions.http(paymentServiceUrl))
                .route(RequestPredicates.path("/api/payment/swagger-ui/**"), HandlerFunctions.http(paymentServiceUrl))
                .route(RequestPredicates.path("/api/payment/swagger-ui.html"), HandlerFunctions.http(paymentServiceUrl))
                .build();
    }
}
