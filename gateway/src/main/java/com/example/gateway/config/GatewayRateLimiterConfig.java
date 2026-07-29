package com.example.gateway.config;
import com.example.gateway.constants.GatewayConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.ServerRequest;

import java.util.function.Function;

@Configuration
public class GatewayRateLimiterConfig {

    /**
     * Resolves rate limiting keys using the client's Remote IP.
     * Used by public endpoints (e.g., /api/auth/**).
     */
    @Bean
    public Function<ServerRequest, String> ipKeyResolver() {
        return request -> request.remoteAddress()
                .map(addr -> "rate:ip:" + addr.getAddress().getHostAddress())
                .orElse("rate:ip:unknown");
    }

    /**
     * Resolves rate limiting keys using down-stream auth headers or token values.
     * Used by internal authenticated microservices.
     */
    @Bean
    public Function<ServerRequest, String> tokenKeyResolver() {
        return request -> {
            // Read from your custom GatewayConstants header string
            String userId = request.headers().firstHeader(GatewayConstants.USER_ID);

            if (userId != null && !userId.isBlank()) {
                return "rate:user:" + userId;
            }

            // Fallback: Manually read Authorization header if interceptor hasn't fired yet
            String authHeader = request.headers().firstHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                // Using hashcode keeps Redis keys small and fast
                return "rate:token:" + authHeader.substring(7).hashCode();
            }

            // Global safe fallback so traffic is capped instead of broken
            return "rate:user:anonymous";
        };
    }
}
