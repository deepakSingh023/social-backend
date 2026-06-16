package com.example.social_chat.Security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@RequiredArgsConstructor
public class InternalFilter extends OncePerRequestFilter {


    private final String token;


    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest req
    ){

        String api = req.getRequestURI();

        return !api.startsWith("/api/conversation/");
    }

    protected void doFilterInternal(
            HttpServletRequest req ,
            HttpServletResponse res,
            FilterChain filterChain
    )throws IOException, ServletException {

        String header = req.getHeader("X-SECRET-TOKEN");

        if(header == null || !header.equals(token)){
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            res.getWriter().write("wrong secret");
            return;
        }

       filterChain.doFilter(req,res);


    }
}
