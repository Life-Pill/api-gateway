package com.lifepill.api_gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Gateway Route Configuration for LifePill Microservices.
 * 
 * Routes are organized by service domain for maintainability.
 * All URIs and paths are externalized to environment variables.
 */
@Slf4j
@Configuration
public class GatewayRouteConfig {

    @Value("${gateway.services.user-auth.name:MOBILE-USER-AUTH-SERVICE}")
    private String userAuthServiceName;

    @Value("${gateway.services.config-server.name:CONFIG-SERVER}")
    private String configServerName;

    @Value("${gateway.services.eureka.uri:http://localhost:8761}")
    private String eurekaUri;

    @Value("${api.version:v1}")
    private String apiVersion;

    @Value("${gateway.headers.source:lifepill-gateway}")
    private String gatewayHeaderSource;

    @Value("${gateway.routes.retry-count:3}")
    private int retryCount;

    @Value("${gateway.services.identity-service.name:IDENTITY-SERVICE}")
    private String identityServiceName;

    /**
     * Configures all gateway routes for the LifePill ecosystem.
     * 
     * @param builder RouteLocatorBuilder provided by Spring Cloud Gateway
     * @return RouteLocator containing all route definitions
     */
    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
        String authPath = "/api/" + apiVersion + "/auth/**";
        String userPath = "/api/" + apiVersion + "/user/**";
        
        log.info("Configuring gateway routes with API version: {}", apiVersion);
        log.info("Auth path: {}, User path: {}", authPath, userPath);
        
        return builder.routes()
                // Auth Routes - /api/v1/auth/**
                .route("user-auth-service", r -> r
                        .path(authPath)
                        .filters(f -> f
                                .circuitBreaker(c -> c
                                        .setName("userAuthCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/auth"))
                                .retry(retryConfig -> retryConfig
                                        .setRetries(retryCount)
                                        .setStatuses(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
                                                org.springframework.http.HttpStatus.BAD_GATEWAY))
                                .addRequestHeader("X-Gateway-Source", gatewayHeaderSource)
                                .addResponseHeader("X-Response-Time", String.valueOf(System.currentTimeMillis())))
                        .uri("lb://" + userAuthServiceName))

                // Identity Service Direct Auth - /lifepill/v1/auth/**
                .route("identity-service-direct-auth", r -> r
                        .path("/lifepill/v1/auth/**")
                        .filters(f -> f
                                .circuitBreaker(c -> c
                                        .setName("identityServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/identity"))
                                .retry(retryConfig -> retryConfig
                                        .setRetries(retryCount)
                                        .setStatuses(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
                                                org.springframework.http.HttpStatus.BAD_GATEWAY))
                                .addRequestHeader("X-Gateway-Source", gatewayHeaderSource))
                        .uri("lb://" + identityServiceName))
                
                // User Profile Routes - /api/v1/user/**
                .route("user-profile-service", r -> r
                        .path(userPath)
                        .filters(f -> f
                                .circuitBreaker(c -> c
                                        .setName("userAuthCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/user"))
                                .addRequestHeader("X-Gateway-Source", gatewayHeaderSource))
                        .uri("lb://" + userAuthServiceName))
                
                // Swagger/OpenAPI Documentation Routes for User Auth
                .route("user-auth-swagger", r -> r
                        .path("/api/v3/api-docs/**", "/api/swagger-ui/**", "/api/swagger-ui.html")
                        .filters(f -> f
                                .addRequestHeader("X-Gateway-Source", gatewayHeaderSource))
                        .uri("lb://" + userAuthServiceName))
                
                // Eureka Dashboard Routes
                .route("eureka-dashboard", r -> r
                        .path("/eureka/web")
                        .filters(f -> f.setPath("/"))
                        .uri(eurekaUri))
                
                .route("eureka-static", r -> r
                        .path("/eureka/**")
                        .uri(eurekaUri))
                
                // Config Server Routes
                .route("config-server", r -> r
                        .path("/config/**")
                        .filters(f -> f.rewritePath("/config/(?<segment>.*)", "/${segment}"))
                        .uri("lb://" + configServerName))
                
                .build();
    }
}
