package com.example.gateway.filter;

import com.example.gateway.constants.GatewayConstants;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

public final class HeaderPropagationFilter {

    private HeaderPropagationFilter() {}

    /**
     * Extracts authenticated details from the Gateway's SecurityContext
     * and maps them to HTTP Request Headers for downstream services.
     */
    public static HandlerFilterFunction<ServerResponse, ServerResponse> propagate() {
        return (request, next) -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            // If no user session is established in the gateway, pass the request as-is
            if (auth == null || !auth.isAuthenticated()) {
                return next.handle(request);
            }

            // Correctly mutate the request with custom user detail headers
            ServerRequest mutatedRequest = ServerRequest.from(request)
                    .header(GatewayConstants.USER_ID, auth.getName())
                    // Example extension if your Authentication principal stores details:
                    // .header(GatewayConstants.USERNAME, auth.getPrincipal().toString())
                    .build();

            // Hand off the mutated request to the next filter or downstream service
            return next.handle(mutatedRequest);
        };
    }
}
