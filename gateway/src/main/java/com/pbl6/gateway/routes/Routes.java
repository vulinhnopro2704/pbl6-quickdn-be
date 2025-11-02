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

    @Bean
    public RouterFunction<ServerResponse> authServiceRoute() {
        // Use leading slashes on paths and the configured service URL (works inside Docker network)
        return GatewayRouterFunctions.route("auth-service")
                .route(RequestPredicates.path("/api/auth/test"), HandlerFunctions.http(authServiceUrl))
                .route(RequestPredicates.path("/api/auth/register"), HandlerFunctions.http(authServiceUrl))
                .route(RequestPredicates.path("/api/auth/login"), HandlerFunctions.http(authServiceUrl))
                .route(RequestPredicates.path("/api/auth/logout"), HandlerFunctions.http(authServiceUrl))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> goongMapServiceRoute() {
        return GatewayRouterFunctions.route("goongmap-service")
                .route(RequestPredicates.path("/api/goongmap/test"), HandlerFunctions.http(goongmapServiceUrl))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> orderServiceRoute() {
        return GatewayRouterFunctions.route("order-service")
                .route(RequestPredicates.path("/api/order/test"), HandlerFunctions.http(orderServiceUrl))
                .route(RequestPredicates.path("/api/order"), HandlerFunctions.http(orderServiceUrl))
                .build();
    }
}
