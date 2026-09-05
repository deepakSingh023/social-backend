package com.example.gateway.config;

import com.example.gateway.constants.GatewayConstants;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.time.Duration;


@Configuration
public class GatewayRouteConfig {

    // Helper method to keep route building clean and scannable
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            ServerHttpRequest request = exchange.getRequest();

            String ipKey = Mono.justOrEmpty(request.getRemoteAddress())
                    .map(InetSocketAddress::getHostString)
                    .map(ip -> "rate:ip:" + ip)
                    .defaultIfEmpty("rate:ip:unknown")
                    .block(); // Safe to block briefly during initial exchange map

            return Mono.just(ipKey);
        };
    }

    @Bean
    public KeyResolver tokenKeyResolver() {
        return exchange -> {
            ServerHttpRequest request = exchange.getRequest();

            // WebFlux reads headers via getHeaders().getFirst()
            String userId = request.getHeaders().getFirst(GatewayConstants.USER_ID);

            if (userId != null && !userId.isBlank()) {
                return Mono.just("rate:user:" + userId);
            }

            String authHeader = request.getHeaders().getFirst("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                int tokenHash = authHeader.substring(7).hashCode();
                return Mono.just("rate:token:" + tokenHash);
            }

            return Mono.just("rate:user:anonymous");
        };
    }




    @Bean
    public RouteLocator gatewayRoutes(
            RouteLocatorBuilder builder,
            GatewayFilter myHeaderFilter,           // Changed from HandlerFilterFunction to WebFlux GatewayFilter
            KeyResolver ipKeyResolver,              // Rewritten reactive bean
            KeyResolver tokenKeyResolver) {         // Rewritten reactive bean

        return builder.routes()
                .route("auth-service", r -> r.path("/api/auth/**")
                        .filters(f -> f.requestRateLimiter(c -> c.setKeyResolver(ipKeyResolver).setRateLimiter(new RedisRateLimiter(20, 20)))
                                .filter(myHeaderFilter))
                        .uri("http://auth-service:8080"))

                .route("post-service", r -> r.path("/api/posts/**")
                        .filters(f -> f.requestRateLimiter(c -> c.setKeyResolver(tokenKeyResolver).setRateLimiter(new RedisRateLimiter(100, 100)))
                                .filter(myHeaderFilter))
                        .uri("http://post-service:8082"))

                .route("profile-service", r -> r.path("/api/profiles/**")
                        .filters(f -> f.requestRateLimiter(c -> c.setKeyResolver(tokenKeyResolver).setRateLimiter(new RedisRateLimiter(60, 60)))
                                .filter(myHeaderFilter))
                        .uri("http://profile-service:8081"))

                .route("feed-service", r -> r.path("/api/getFeed/**")
                        .filters(f -> f.requestRateLimiter(c -> c.setKeyResolver(tokenKeyResolver).setRateLimiter(new RedisRateLimiter(200, 200)))
                                .filter(myHeaderFilter))
                        .uri("http://feed-service:8086"))

                .route("likes-service", r -> r.path("/api/like/**", "/api/comments")
                        .filters(f -> f.requestRateLimiter(c -> c.setKeyResolver(tokenKeyResolver).setRateLimiter(new RedisRateLimiter(80, 80)))
                                .filter(myHeaderFilter))
                        .uri("http://likes-service:8083"))

                .route("interaction-service", r -> r.path("/api/relations/**", "/api/friends/**", "/api/search/**")
                        .filters(f -> f.requestRateLimiter(c -> c.setKeyResolver(tokenKeyResolver).setRateLimiter(new RedisRateLimiter(80, 80)))
                                .filter(myHeaderFilter))
                        .uri("http://interaction-service:8085"))

                .route("reel-service", r -> r.path("/api/reels/**")
                        .filters(f -> f.requestRateLimiter(c -> c.setKeyResolver(tokenKeyResolver).setRateLimiter(new RedisRateLimiter(120, 120)))
                                .filter(myHeaderFilter))
                        .uri("http://reel-service:8084"))

                .route("view-service", r -> r.path("/api/view/**")
                        .filters(f -> f.requestRateLimiter(c -> c.setKeyResolver(tokenKeyResolver).setRateLimiter(new RedisRateLimiter(150, 150)))
                                .filter(myHeaderFilter))
                        .uri("http://view-service:8087"))

                .route("reelfetch-service", r -> r.path("/api/reels/**")
                        .filters(f -> f.requestRateLimiter(c -> c.setKeyResolver(tokenKeyResolver).setRateLimiter(new RedisRateLimiter(200, 200)))
                                .filter(myHeaderFilter))
                        .uri("http://reelfetch-service:8089"))

                .route("chat-api-service", r -> r.path("/api/chat/**")
                        .filters(f -> f.requestRateLimiter(c -> c.setKeyResolver(tokenKeyResolver).setRateLimiter(new RedisRateLimiter(60, 60)))
                                .filter(myHeaderFilter))
                        .uri("http://chat-service:8090"))

                // THE NATIVE WEBSOCKET FIX: Sockets will now connect seamlessly under Swarm scaling
                .route("chat-websocket-service", r -> r.path("/ws/**")
                        .filters(f -> f.filter(myHeaderFilter))
                        .uri("ws://chat-service:8090"))
                .build();
    }
}