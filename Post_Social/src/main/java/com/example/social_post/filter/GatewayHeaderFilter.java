package com.example.social_post.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.rmi.ServerException;


@RequiredArgsConstructor
public class GatewayHeaderFilter extends OncePerRequestFilter {


    private final  String expectedSecret;


    @Override
    public void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain filterChain)throws IOException, ServletException {

        String api = req.getRequestURI();

        if(api.startsWith("/api/denormalize/") || api.startsWith("/api/post/") || api.startsWith("/api/health") || api.startsWith("/actuator")
        || api.startsWith("/api/posts/users")){
            filterChain.doFilter(req,res);
            return;
        }

        String clientSecret = req.getHeader("X-Gateway-Secret");

        // 3. Verify the secret matches
        if (expectedSecret == null || !expectedSecret.equals(clientSecret)) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            res.setContentType("application/json");
            res.getWriter().write("{\"error\": \"Direct access forbidden. Requests must pass through the API Gateway.\"}");
            return; // Halt the filter chain execution right here
        }

        // 4. Continue to the next filter/controller if secret is valid
        filterChain.doFilter(req, res);

    }
}
