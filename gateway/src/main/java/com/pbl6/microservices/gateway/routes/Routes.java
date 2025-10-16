package com.pbl6.microservices.gateway.routes;

import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.*;

@Configuration
public class Routes {
    @Bean
    public RouterFunction<ServerResponse> orderServiceRoute() {
        return GatewayRouterFunctions.route("order-service")
                .route(RequestPredicates.path("api/order/test"), HandlerFunctions.http("http://localhost:8081"))
                .route(RequestPredicates.path("api/order"), HandlerFunctions.http("http://localhost:8081"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> authServiceRoute() {
        return GatewayRouterFunctions.route("auth-service")
                .route(RequestPredicates.path("api/auth/test"), HandlerFunctions.http("http://localhost:8082"))
                .route(RequestPredicates.path("api/auth/register"), HandlerFunctions.http("http://localhost:8082"))
                .route(RequestPredicates.path("api/auth/login"), HandlerFunctions.http("http://localhost:8082"))
                .route(RequestPredicates.path("api/auth/logout"), HandlerFunctions.http("http://localhost:8082"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> goongMapServiceRoute() {
        return GatewayRouterFunctions.route("goongmap-service")
                .route(RequestPredicates.path("api/goongmap/test"), HandlerFunctions.http("http://localhost:8083"))
                .build();
    }

}
