package com.example.social_post.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class InternalFilter extends OncePerRequestFilter {

    private final String localSecret;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String uri = request.getRequestURI();

        return !(uri.startsWith("/api/denormalize/")
                || uri.startsWith("/api/post/"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)throws IOException, ServletException{

        String secret = request.getHeader("X-SECRET-TOKEN");

        if(secret ==null || !secret.equals(localSecret)){

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("incorrect secret");
            return;

        }

        filterChain.doFilter(request,response);

    }

}
