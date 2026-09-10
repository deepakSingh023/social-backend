package com.example.Social.profile.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@RequiredArgsConstructor
public class GateWayHeaderFilter extends OncePerRequestFilter {

    private final String expectedSecret;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (request.getRequestURI().startsWith("/actuator")
                || request.getRequestURI().startsWith("/api/controller/counter/")
                || request.getRequestURI().startsWith("/api/profiles/get/")
                || request.getRequestURI().startsWith("/api/profiles/create")
                || request.getRequestURI().startsWith("/api/profiles/search")
                || request.getRequestURI().startsWith("/api/profiles/fetch-profile-else/")
                || request.getRequestURI().startsWith("/api/health")) {

            filterChain.doFilter(request, response);
            return;
        }

        String clientSecret = request.getHeader("X-Gateway-Secret");


        if (expectedSecret == null || !expectedSecret.equals(clientSecret)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Direct access forbidden. Requests must pass through the API Gateway.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}

