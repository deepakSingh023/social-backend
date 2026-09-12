package com.example.social_fetchReels.config;

import com.example.social_fetchReels.filter.GatewayHeaderFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public GatewayHeaderFilter gatewayHeaderFilter(@Value("${service.secret.gateway}")String gatewaySecret){
        return new GatewayHeaderFilter(gatewaySecret);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,GatewayHeaderFilter gatewayHeaderFilter
                                                   ) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS,"/**").permitAll()
                        .anyRequest().permitAll()
                )
                .addFilterBefore(gatewayHeaderFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }



}
