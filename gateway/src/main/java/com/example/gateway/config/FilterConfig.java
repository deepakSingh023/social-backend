package com.example.gateway.config;
import com.example.gateway.filter.HeaderPropagationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.ServerResponse;

@RequiredArgsConstructor
@Configuration

public class FilterConfig {

    private final HeaderPropagationFilter headerPropagationFilter;

    @Bean
    public HandlerFilterFunction<ServerResponse, ServerResponse> myHeaderFilter() {
        return headerPropagationFilter.propagate();
    }
}