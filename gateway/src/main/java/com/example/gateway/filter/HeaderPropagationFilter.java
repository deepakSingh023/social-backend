package com.example.gateway.filter;

import com.example.gateway.constants.GatewayConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;

@Component
public class HeaderPropagationFilter {

    @Value("${app.gateway.secret}")
    private String gatewaySecret;

    public GatewayFilter propagate() {
        return (exchange, chain) ->
                ReactiveSecurityContextHolder.getContext()
                        .map(SecurityContext::getAuthentication)
                        .filter(auth -> auth != null && auth.isAuthenticated())
                        .map(auth -> {
                            // CORRECT WEBFLUX METHOD: Call .mutate() to update headers
                            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                                    .header(GatewayConstants.USER_ID, auth.getName())
                                    .header(GatewayConstants.GATEWAY_SECRET, gatewaySecret)
                                    .build();

                            return exchange.mutate().request(mutatedRequest).build();
                        })
                        .defaultIfEmpty(exchange)
                        .flatMap(chain::filter);
    }
}
