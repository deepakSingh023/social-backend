package com.example.social_likes.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.rmi.ServerException;


@RequiredArgsConstructor
public class InternalFilter extends OncePerRequestFilter {


    private final String token;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest req){

        String url = req.getRequestURI();

        return !(url.startsWith("/api/likes") ||
                url.startsWith("/api/comments/denormalize/"));

    }


    @Override
    protected void doFilterInternal(
            HttpServletRequest req,
            HttpServletResponse res,
            FilterChain filterChain
    )throws IOException, ServletException {


        String secret = req.getHeader("X-SECRET-TOKEN");

        if(secret == null || !secret.equals(token)){

            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            res.getWriter().write("wrong internal token");
            return;

        }

        filterChain.doFilter(req,res);

    }
}
