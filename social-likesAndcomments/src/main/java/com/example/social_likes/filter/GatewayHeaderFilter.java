package com.example.social_likes.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class GatewayHeaderFilter extends OncePerRequestFilter {

    private final String gatewaySecret;

    @Override
    public void doFilterInternal(
            HttpServletRequest req,
            HttpServletResponse res,
            FilterChain filterChain
    )throws IOException, ServletException {

            String api = req.getRequestURI();

            if(api.startsWith("/actuator") || api.startsWith("/api/comments/denormalize/cleanup") || api.startsWith("/api/health") || api.startsWith("/api/likes/")
            || api.startsWith("/api/post/")){
                filterChain.doFilter(req,res);
                return;
            }

            String clientSecret = req.getHeader("X-Gateway-Secret");


        if (gatewaySecret == null || !gatewaySecret.equals(clientSecret)) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            res.setContentType("application/json");
            res.getWriter().write("{\"error\": \"Direct access forbidden. Requests must pass through the API Gateway.\"}");
            return;
        }

        filterChain.doFilter(req, res);
    }
}
