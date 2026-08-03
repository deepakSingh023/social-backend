package com.example.gateway.filter;
import com.example.gateway.constants.GatewayConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

@Component
public class HeaderPropagationFilter {

    @Value("${app.gateway.secret}")
    private String gatewaySecret;

    public HandlerFilterFunction<ServerResponse, ServerResponse> propagate() {

        return (request, next) -> {

            Authentication auth =
                    SecurityContextHolder.getContext().getAuthentication();

            if (auth == null || !auth.isAuthenticated()) {
                return next.handle(request);
            }

            ServerRequest mutatedRequest =
                    ServerRequest.from(request)
                            .header(GatewayConstants.USER_ID, auth.getName())
                            .header(GatewayConstants.GATEWAY_SECRET, gatewaySecret)
                            .build();

            return next.handle(mutatedRequest);
        };
    }
}