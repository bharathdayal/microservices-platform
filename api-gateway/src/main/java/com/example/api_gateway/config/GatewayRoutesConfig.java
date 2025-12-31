package com.example.api_gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.Principal;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {

        return builder.routes()

                // ---- Auth Service ----
                .route("auth-microservice",r->r
                        .path("/auth/**")
                        .filters(f->f
                                .circuitBreaker(c->c
                                        .setName("auth-microservice")
                                        .setFallbackUri("forward:/fallback/auth")

                                )
                        )
                        .uri("http://localhost:8096")
                )

                // ---- Order Service ----
                .route("order-service", r -> r
                        .path("/api/orders/**")
                        .filters(f -> f
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(userKeyResolver())
                                )
                        )
                        .uri("http://localhost:8097")
                )

                // ---- DLT ADMIN Service ----
                .route("dlt-replay-service", r -> r
                        .path("/admin/dlt/**")
                        .filters(f -> f
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(userKeyResolver())
                                )
                        )
                        .uri("http://localhost:8088")
                )

                .build();
    }

    /**
     * 5 req/sec with burst of 10
     */

    @Bean
    RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(5,10);
    }

    /**
     * Rate limit per authenticated principal
     */
    @Bean
    KeyResolver userKeyResolver() {
        return exchange ->
                exchange.getPrincipal()
                        .map(Principal::getName)
                        .defaultIfEmpty("anonymous");
    }

}
