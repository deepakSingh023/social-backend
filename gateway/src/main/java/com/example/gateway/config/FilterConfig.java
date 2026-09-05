package com.example.gateway.config;

import com.example.gateway.filter.HeaderPropagationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cloud.gateway.filter.GatewayFilter;

@RequiredArgsConstructor
@Configuration
public class FilterConfig {

    private final HeaderPropagationFilter headerPropagationFilter;

    @Bean
    public GatewayFilter myHeaderFilter() {
        // Change propagate() return signature in HeaderPropagationFilter to implement GatewayFilter
        return headerPropagationFilter.propagate();
    }
}
