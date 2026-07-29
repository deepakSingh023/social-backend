package com.example.gateway.config;

import com.example.gateway.filter.LoggingFilters;
import com.example.gateway.filter.GatewayMetrics;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class GlobalLoggingConfig {

    @Bean
    public HandlerFilterFunction<ServerResponse, ServerResponse> globalLoggingFilter(GatewayMetrics metrics) {
        return LoggingFilters.audit(metrics);
    }
}
