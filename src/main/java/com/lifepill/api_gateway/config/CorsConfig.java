package com.lifepill.api_gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * CORS Configuration for API Gateway.
 * 
 * Provides centralized CORS configuration for all downstream services.
 * All values are externalized to environment variables.
 */
@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:8081}")
    private String allowedOrigins;

    @Value("${cors.allowed-methods:GET,POST,PUT,PATCH,DELETE,OPTIONS}")
    private String allowedMethods;

    @Value("${cors.allowed-headers:Authorization,Content-Type,Accept,Origin,X-Requested-With}")
    private String allowedHeaders;

    @Value("${cors.exposed-headers:Authorization,X-Response-Time,X-Request-Id}")
    private String exposedHeaders;

    @Value("${cors.allow-credentials:true}")
    private boolean allowCredentials;

    @Value("${cors.max-age:3600}")
    private long maxAge;

    /**
     * Configures CORS for the API Gateway.
     * This configuration applies to all routes passing through the gateway.
     * 
     * @return CorsWebFilter with production-ready CORS settings
     */
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        
        // Allowed origins from environment
        corsConfig.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        
        // Allowed HTTP methods from environment
        corsConfig.setAllowedMethods(Arrays.asList(allowedMethods.split(",")));
        
        // Allowed headers from environment
        List<String> headers = Arrays.asList(allowedHeaders.split(","));
        corsConfig.setAllowedHeaders(headers);
        
        // Exposed headers from environment
        corsConfig.setExposedHeaders(Arrays.asList(exposedHeaders.split(",")));
        
        // Allow credentials (cookies, authorization headers)
        corsConfig.setAllowCredentials(allowCredentials);
        
        // Cache preflight response
        corsConfig.setMaxAge(maxAge);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        
        return new CorsWebFilter(source);
    }
}
