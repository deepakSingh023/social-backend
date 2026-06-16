package com.example.Friend_Feed.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class InternalFilter extends OncePerRequestFilter {

    private final String localSecret;

    private final static Logger log = LoggerFactory.getLogger(InternalFilter.class);

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request){

        String uri = request.getRequestURI();

        log.info("shouldNotFilter={}", !uri.startsWith("/api/feeds/"));

        return !uri.startsWith("/api/feeds/");

    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    )throws IOException, ServletException{

        String secret = request.getHeader("X-SECRET-TOKEN");

        log.info("internal secret = {}",secret);

        if(secret == null || !secret.equals(localSecret)){
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("wrong secret for inter service call");
            return;
        }

        filterChain.doFilter(request,response);


    }
}
