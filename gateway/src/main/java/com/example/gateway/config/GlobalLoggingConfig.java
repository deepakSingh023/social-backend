package com.example.gateway.config;

import com.example.gateway.filter.LoggingFilters;
import com.example.gateway.filter.GatewayMetrics;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cloud.gateway.filter.GlobalFilter;

@Configuration
public class GlobalLoggingConfig {

    @Bean
    public GlobalFilter globalLoggingFilter(GatewayMetrics metrics) {
        // LoggingFilters.audit should return a GlobalFilter wrapper
        return LoggingFilters.audit(metrics);
    }
}
