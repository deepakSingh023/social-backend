package com.example.social_interest.filter;

import com.example.social_interest.aspect.LogAspect;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@RequiredArgsConstructor
public class InternalFilter extends OncePerRequestFilter {

    private final String secret;

    private final static Logger log = LoggerFactory.getLogger(InternalFilter.class);



    @Override
    protected boolean shouldNotFilter(HttpServletRequest request){

        String uri = request.getRequestURI();

        return !uri.startsWith("/api/interests/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)throws IOException, ServletException {

        String token = request.getHeader("X-SECRET-TOKEN");

        log.info("internal secret = {}",secret);

        log.info("user token = {}",token);

        if(token == null || !token.equals(secret)){

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("wrong internal secret");
            return;

        }

        filterChain.doFilter(request,response);
    }
}
