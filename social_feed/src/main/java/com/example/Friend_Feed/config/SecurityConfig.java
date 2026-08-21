package com.example.Friend_Feed.config;


import com.example.Friend_Feed.filter.GatewayHeaderFilter;
import com.example.Friend_Feed.filter.InternalFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;


@EnableMethodSecurity
@Configuration
public class SecurityConfig {

    @Bean
    public GatewayHeaderFilter gatewayHeaderFilter(@Value("${service.secret.gateway}")String gatewaySecret){
        return new GatewayHeaderFilter(gatewaySecret);
    }

    @Bean
    public InternalFilter internalFilter(@Value("${service.secret}") String secret){
        return new InternalFilter(secret);
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   GatewayHeaderFilter gatewayHeaderFilter, InternalFilter internalFilter) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().permitAll()
                )
                .addFilterBefore(gatewayHeaderFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(internalFilter, GatewayHeaderFilter.class);

        return http.build();
    }



}
