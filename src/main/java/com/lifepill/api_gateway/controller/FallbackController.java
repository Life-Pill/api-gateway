package com.lifepill.api_gateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Fallback Controller for Circuit Breaker.
 * 
 * Provides graceful degradation when downstream services are unavailable.
 * 
 * Fallback responses provide:
 * - User-friendly error messages
 * - Appropriate HTTP status codes
 * - Structured error response format
 */
@Slf4j
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    /**
     * Generic service fallback handler.
     * Returns a 503 Service Unavailable with helpful message.
     * 
     * @return Mono<ResponseEntity<Map<String, Object>>> with error details
     */
    @GetMapping(value = "/service", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Map<String, Object>>> serviceFallback() {
        log.warn("Service fallback triggered - downstream service unavailable");
        
        Map<String, Object> response = createFallbackResponse(
                "SERVICE_UNAVAILABLE",
                "The requested service is temporarily unavailable. Please try again later.",
                "service"
        );
        
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    /**
     * Authentication service fallback handler.
     * Auth service failures should prompt retry with different message.
     * 
     * @return Mono<ResponseEntity<Map<String, Object>>> with auth-specific error
     */
    @GetMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Map<String, Object>>> authFallback() {
        log.warn("Auth service fallback triggered - authentication service unavailable");
        
        Map<String, Object> response = createFallbackResponse(
                "AUTH_SERVICE_UNAVAILABLE",
                "Authentication service is temporarily unavailable. Please try again in a few moments.",
                "auth"
        );
        
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    /**
     * User service fallback handler.
     * 
     * @return Mono<ResponseEntity<Map<String, Object>>> with user service error
     */
    @GetMapping(value = "/user", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Map<String, Object>>> userFallback() {
        log.warn("User service fallback triggered - user service unavailable");
        
        Map<String, Object> response = createFallbackResponse(
                "USER_SERVICE_UNAVAILABLE",
                "User service is temporarily unavailable. Your data is safe. Please try again later.",
                "user"
        );
        
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    /**
     * Rate limit exceeded fallback.
     * 
     * @return Mono<ResponseEntity<Map<String, Object>>> with rate limit error
     */
    @GetMapping(value = "/rate-limited", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Map<String, Object>>> rateLimitFallback() {
        log.warn("Rate limit fallback triggered");
        
        Map<String, Object> response = createFallbackResponse(
                "RATE_LIMIT_EXCEEDED",
                "Too many requests. Please slow down and try again later.",
                "rate-limit"
        );
        
        return Mono.just(ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response));
    }

    /**
     * Creates a standardized fallback response.
     * 
     * @param code Error code for client handling
     * @param message Human-readable error message
     * @param source Service source of the fallback
     * @return Map containing structured error response
     */
    private Map<String, Object> createFallbackResponse(String code, String message, String source) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", code);
        response.put("message", message);
        response.put("source", source);
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("suggestion", "If this problem persists, please contact support@lifepill.com");
        
        return response;
    }
}
