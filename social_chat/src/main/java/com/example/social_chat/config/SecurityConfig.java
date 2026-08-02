package com.example.social_chat.config;

import com.example.social_chat.Security.GatewayHeaderFilter;
import com.example.social_chat.Security.InternalFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {


    @Bean
    public InternalFilter internalFilter(@Value("${service.secret}")String token){
        return new InternalFilter(token);
    }


    @Bean
    public GatewayHeaderFilter gatewayHeaderFilter(@Value("${service.secret.gateway}")String gatewaySecret){
        return new GatewayHeaderFilter(gatewaySecret);
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,GatewayHeaderFilter gatewayHeaderFilter, InternalFilter internalFilter) throws Exception {

        http
                .cors(Customizer.withDefaults()) // ✅ ENABLE CORS
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS,"/**").permitAll()
                        .anyRequest().permitAll()
                )
                .addFilterBefore(gatewayHeaderFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(internalFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
