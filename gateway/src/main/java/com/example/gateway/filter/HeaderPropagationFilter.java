package com.example.gateway.filter;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;

import com.example.gateway.constants.GatewayConstants;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.function.ServerRequest;

import java.util.function.Function;

public final class HeaderPropagationFilter {

    private HeaderPropagationFilter(){}

    public static Function<ServerRequest, ServerRequest> propagate() {

        return request -> {

            Authentication auth =
                    SecurityContextHolder.getContext().getAuthentication();

            if (auth == null || !auth.isAuthenticated()) {
                return request;
            }

            return ServerRequest.from(request)
                    .header(GatewayConstants.USER_ID, auth.getName())
                    .build();
        };
    }
}