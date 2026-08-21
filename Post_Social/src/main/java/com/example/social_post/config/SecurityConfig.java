package com.example.social_post.config;

import com.example.social_post.filter.GatewayHeaderFilter;
import com.example.social_post.filter.InternalFilter;
import lombok.RequiredArgsConstructor;
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

@RequiredArgsConstructor
@Configuration
@EnableMethodSecurity
public class SecurityConfig {


    @Bean
    public InternalFilter internalFilter(
            @Value("${service.secret}") String secret) {
        return new InternalFilter(secret);
    }

    @Bean
    public GatewayHeaderFilter gatewayHeaderFilter(@Value("${service.gateway.secret}") String gatewaySecret) {
        return new GatewayHeaderFilter(gatewaySecret);
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   InternalFilter internalFilter, GatewayHeaderFilter gatewayHeaderFilter) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().permitAll() // Trusting your filters to protect the app boundaries
                );

        // Put both border guards side-by-side at the front door
        http.addFilterBefore(gatewayHeaderFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(internalFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
