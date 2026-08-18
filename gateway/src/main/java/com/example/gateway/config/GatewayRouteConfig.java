package com.example.gateway.config;

import com.example.gateway.constants.GatewayConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import org.springframework.web.servlet.function.HandlerFilterFunction;

// Correct static imports for Spring Cloud Gateway MVC
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.cloud.gateway.server.mvc.filter.FilterFunctions.stripPrefix;
import static org.springframework.cloud.gateway.server.mvc.filter.Bucket4jFilterFunctions.rateLimit;

import java.util.function.Function;

import java.time.Duration;

@Configuration
public class GatewayRouteConfig {

    public Function<ServerRequest, String> ipKeyResolver() {
        return request -> request.remoteAddress()
                .map(addr -> "rate:ip:" + addr.getAddress().getHostAddress())
                .orElse("rate:ip:unknown");
    }

    public Function<ServerRequest, String> tokenKeyResolver() {
        return request -> {
            String userId = request.headers().firstHeader(GatewayConstants.USER_ID);
            if (userId != null && !userId.isBlank()) {
                return "rate:user:" + userId;
            }
            String authHeader = request.headers().firstHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                return "rate:token:" + authHeader.substring(7).hashCode();
            }
            return "rate:user:anonymous";
        };
    }

    @Bean
    public RouterFunction<ServerResponse> gatewayRoutes(HandlerFilterFunction<ServerResponse, ServerResponse> myHeaderFilter) {

        // THE FIX: We pass concrete capacity configurations directly inside the public Consumer layout
        return route("auth-service")
                .route(org.springframework.web.servlet.function.RequestPredicates.path("/api/auth/**"), http("http://auth-service:8080"))
                .filter(rateLimit(c -> c
                        .setKeyResolver(ipKeyResolver())
                        .setCapacity(20)          // Explicitly pass positive primitives
                        .setPeriod(Duration.ofMinutes(1))))
                .filter(myHeaderFilter)
                .build()

                .and(route("post-service")
                        .route(org.springframework.web.servlet.function.RequestPredicates.path("/api/posts/**"), http("http://post-service:8082"))
                        .filter(stripPrefix(2))
                        .filter(rateLimit(c -> c
                                .setKeyResolver(tokenKeyResolver())
                                .setCapacity(100)
                                .setPeriod(Duration.ofMinutes(1))))
                        .filter(myHeaderFilter)
                        .build())

                .and(route("profile-service")
                        .route(org.springframework.web.servlet.function.RequestPredicates.path("/api/profile/**"), http("http://profile-service:8081"))
                        .filter(stripPrefix(2))
                        .filter(rateLimit(c -> c
                                .setKeyResolver(tokenKeyResolver())
                                .setCapacity(60)
                                .setPeriod(Duration.ofMinutes(1))))
                        .build())

                .and(route("feed-service")
                        .route(org.springframework.web.servlet.function.RequestPredicates.path("/api/feed/**"), http("http://feed-service:8086"))
                        .filter(stripPrefix(2))
                        .filter(rateLimit(c -> c
                                .setKeyResolver(tokenKeyResolver())
                                .setCapacity(200)
                                .setPeriod(Duration.ofMinutes(1))))
                        .build())

                .and(route("likes-service")
                        .route(org.springframework.web.servlet.function.RequestPredicates.path("/api/likes/**"), http("http://likes-service:8083"))
                        .filter(stripPrefix(2))
                        .filter(rateLimit(c -> c
                                .setKeyResolver(tokenKeyResolver())
                                .setCapacity(80)
                                .setPeriod(Duration.ofMinutes(1))))
                        .filter(myHeaderFilter)
                        .build())

                .and(route("interaction-service")
                        .route(org.springframework.web.servlet.function.RequestPredicates.path("/api/interactions/**"), http("http://interaction-service:8085"))
                        .filter(stripPrefix(2))
                        .filter(rateLimit(c -> c
                                .setKeyResolver(tokenKeyResolver())
                                .setCapacity(80)
                                .setPeriod(Duration.ofMinutes(1))))
                        .filter(myHeaderFilter)
                        .build())

                .and(route("reel-service")
                        .route(org.springframework.web.servlet.function.RequestPredicates.path("/api/reels/**"), http("http://reel-service:8084"))
                        .filter(stripPrefix(2))
                        .filter(rateLimit(c -> c
                                .setKeyResolver(tokenKeyResolver())
                                .setCapacity(120)
                                .setPeriod(Duration.ofMinutes(1))))
                        .filter(myHeaderFilter)
                        .build())

                .and(route("view-service")
                        .route(org.springframework.web.servlet.function.RequestPredicates.path("/api/views/**"), http("http://view-service:8087"))
                        .filter(stripPrefix(2))
                        .filter(rateLimit(c -> c
                                .setKeyResolver(tokenKeyResolver())
                                .setCapacity(150)
                                .setPeriod(Duration.ofMinutes(1))))
                        .filter(myHeaderFilter)
                        .build())

                .and(route("reelfetch-service")
                        .route(org.springframework.web.servlet.function.RequestPredicates.path("/api/reelfetch/**"), http("http://reelfetch-service:8089"))
                        .filter(stripPrefix(2))
                        .filter(rateLimit(c -> c
                                .setKeyResolver(tokenKeyResolver())
                                .setCapacity(200)
                                .setPeriod(Duration.ofMinutes(1))))
                        .filter(myHeaderFilter)
                        .build())

                .and(route("chat-service")
                        .route(org.springframework.web.servlet.function.RequestPredicates.path("/ws/**"), http("ws://chat-service:8090"))
                        .filter(myHeaderFilter)
                        .build());
    }
}

