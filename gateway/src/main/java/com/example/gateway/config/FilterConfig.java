package com.example.gateway.config;
import com.example.gateway.filter.HeaderPropagationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class FilterConfig {

    // This creates a tool named "myHeaderFilter"
    @Bean
    public HandlerFilterFunction<ServerResponse, ServerResponse> myHeaderFilter() {
        return HeaderPropagationFilter.propagate();
    }
}
