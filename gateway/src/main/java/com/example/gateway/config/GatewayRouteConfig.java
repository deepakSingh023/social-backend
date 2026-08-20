package com.example.gateway.config;

import com.example.gateway.constants.GatewayConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.List;
import java.util.function.Function;

import static org.springframework.cloud.gateway.server.mvc.filter.AfterFilterFunctions.DedupeStrategy.RETAIN_FIRST;
import static org.springframework.cloud.gateway.server.mvc.filter.Bucket4jFilterFunctions.rateLimit;
import static org.springframework.cloud.gateway.server.mvc.filter.FilterFunctions.dedupeResponseHeader;
import static org.springframework.cloud.gateway.server.mvc.filter.FilterFunctions.stripPrefix;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;

@Configuration
public class GatewayRouteConfig {

    // Helper method to keep route building clean and scannable
    private Function<ServerRequest, String> ipKeyResolver() {
        return request -> request.remoteAddress()
                .map(InetSocketAddress::getHostString)
                .map(ip -> "rate:ip:" + ip)
                .orElse("rate:ip:unknown");
    }

    private Function<ServerRequest, String> tokenKeyResolver() {
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
    public RouterFunction<ServerResponse> gatewayRoutes(
            HandlerFilterFunction<ServerResponse, ServerResponse> myHeaderFilter){

        return route("auth-service")
                .route(RequestPredicates.path("/api/auth/**"), http("http://auth-service:8080"))
                .filter(rateLimit(c -> c
                        .setKeyResolver(ipKeyResolver())
                        .setCapacity(20)
                        .setPeriod(Duration.ofMinutes(1))))
                .filter(myHeaderFilter)
                .build()

                .and(route("post-service")
                        .route(RequestPredicates.path("/api/posts/**"), http("http://post-service:8082"))
                        .filter(rateLimit(c -> c
                                .setKeyResolver(tokenKeyResolver())
                                .setCapacity(100)
                                .setPeriod(Duration.ofMinutes(1))))
                        .filter(myHeaderFilter)
                        .build())

                .and(route("profile-service")
                        .route(RequestPredicates.path("/api/profiles/**"), http("http://profile-service:8081"))
                        .filter(rateLimit(c -> c
                                .setKeyResolver(tokenKeyResolver())
                                .setCapacity(60)
                                .setPeriod(Duration.ofMinutes(1))))
                        .filter(myHeaderFilter)
                        .build())

                .and(route("feed-service")
                        .route(RequestPredicates.path("/api/feed/**"), http("http://feed-service:8086"))
                        .filter(rateLimit(c -> c
                                .setKeyResolver(tokenKeyResolver())
                                .setCapacity(200)
                                .setPeriod(Duration.ofMinutes(1))))
                        .filter(myHeaderFilter)
                        .build())

                .and(route("likes-service")
                        .route(RequestPredicates.path("/api/likes/**"), http("http://likes-service:8083"))
                        .filter(rateLimit(c -> c
                                .setKeyResolver(tokenKeyResolver())
                                .setCapacity(80)
                                .setPeriod(Duration.ofMinutes(1))))
                        .filter(myHeaderFilter)
                        .build())

                .and(route("interaction-service")
                        .route(RequestPredicates.path("/api/interactions/**"), http("http://interaction-service:8085"))
                        .filter(rateLimit(c -> c
                                .setKeyResolver(tokenKeyResolver())
                                .setCapacity(80)
                                .setPeriod(Duration.ofMinutes(1))))
                        .filter(myHeaderFilter)
                        .build())

                .and(route("reel-service")
                        .route(RequestPredicates.path("/api/reels/**"), http("http://reel-service:8084"))
                        .filter(rateLimit(c -> c
                                .setKeyResolver(tokenKeyResolver())
                                .setCapacity(120)
                                .setPeriod(Duration.ofMinutes(1))))
                        .filter(myHeaderFilter)
                        .build())

                .and(route("view-service")
                        .route(RequestPredicates.path("/api/views/**"), http("http://view-service:8087"))
                        .filter(rateLimit(c -> c
                                .setKeyResolver(tokenKeyResolver())
                                .setCapacity(150)
                                .setPeriod(Duration.ofMinutes(1))))
                        .filter(myHeaderFilter)
                        .build())

                .and(route("reelfetch-service")
                        .route(RequestPredicates.path("/api/reelfetch/**"), http("http://reelfetch-service:8089"))
                        .filter(rateLimit(c -> c
                                .setKeyResolver(tokenKeyResolver())
                                .setCapacity(200)
                                .setPeriod(Duration.ofMinutes(1))))
                        .filter(myHeaderFilter)
                        .build())

                .and(route("chat-service")
                        .route(RequestPredicates.path("/ws/**"), http("ws://chat-service:8090"))
                        .filter(myHeaderFilter)
                        .build());
    }
}