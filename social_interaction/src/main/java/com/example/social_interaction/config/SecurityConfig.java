package com.example.social_interaction.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {


    @Bean
    public GatewayHeaderFilter gatewayHeaderFilter(@Value("${service.secret.gateway}")String gatewaySecret){
        return new GatewayHeaderFilter(gatewaySecret);
    }

    @Bean
    public InternalFilter internalFilter(@Value("${service.secret}")String localSecret){
        return new InternalFilter(localSecret);
    }



    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, GatewayHeaderFilter gatewayHeaderFilter, InternalFilter internalFilter) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().permitAll()
                )
                .addFilterBefore(internalFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(gatewayHeaderFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:3000"
        ));
        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "OPTIONS"
        ));
        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type"
        ));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
