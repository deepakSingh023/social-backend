package com.example.gateway.config;

import com.example.gateway.constants.GatewayConstants;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.util.Optional;

@Configuration
public class RateLimiterConfig {

    @Bean(name = "ipKeyResolver")
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            ServerHttpRequest request = exchange.getRequest();
            String ipKey = Optional.ofNullable(request.getRemoteAddress())
                    .map(InetSocketAddress::getHostString)
                    .map(ip -> "rate:ip:" + ip)
                    .orElse("rate:ip:unknown");
            return Mono.just(ipKey);
        };
    }

    @Bean(name = "tokenKeyResolver")
    @Primary
    public KeyResolver tokenKeyResolver() {
        return exchange -> {
            ServerHttpRequest request = exchange.getRequest();
            String userId = request.getHeaders().getFirst(GatewayConstants.USER_ID);

            if (userId != null && !userId.isBlank()) {
                return Mono.just("rate:user:" + userId);
            }

            String authHeader = request.getHeaders().getFirst("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                int tokenHash = authHeader.substring(7).hashCode();
                return Mono.just("rate:token:" + tokenHash);
            }
            return Mono.just("rate:user:anonymous");
        };
    }
}
