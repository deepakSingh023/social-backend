package com.example.social_view.filter;

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

    private final String token;

    private final static Logger log = LoggerFactory.getLogger(InternalFilter.class);


    @Override
    protected boolean shouldNotFilter(HttpServletRequest request){

        String uri = request.getRequestURI();
        log.info("uri = {}",uri);
        return !uri.startsWith("/api/view/create-like-interest");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest req,
            HttpServletResponse res,
            FilterChain filterChain
    )throws IOException, ServletException {

        String header = req.getHeader("X-SECRET-TOKEN");

        log.info("user token = {} , internal secret = {}",header,token);

        if (header == null || !header.equals(token)) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            res.getWriter().write("wrong secret token");
            return;
        }

        filterChain.doFilter(req,res);

    }
}
