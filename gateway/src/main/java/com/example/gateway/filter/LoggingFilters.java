package com.example.gateway.filter;

import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
public final class LoggingFilters {

    private static final Logger log = LoggerFactory.getLogger(LoggingFilters.class);

    private LoggingFilters() {}

    public static GlobalFilter audit(GatewayMetrics metrics) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // Extract internal attributes using WebFlux ServerWebExchangeUtils
            String routeId = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
            if (routeId == null) {
                routeId = "unknown-route";
            }

            URI targetUriObj = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);
            String targetUri = (targetUriObj != null) ? targetUriObj.toString() : "unknown-uri";

            // Start the processing stopwatch
            Timer.Sample stopwatch = metrics.startRequestTimer();

            log.info("GATEWAY INBOUND: Method={} | Path={} | TargetedService={} | TargetURI={}",
                    request.getMethod(), request.getPath(), routeId, targetUri);

            final String finalRouteId = routeId;

            // Execute the reactive flow and log performance upon completion/termination
            return chain.filter(exchange)
                    .doOnError(ex -> {
                        metrics.recordRoutingStatus(finalRouteId, 500);
                        log.error("GATEWAY ERROR: Request failed routing to service={}. Error={}", finalRouteId, ex.getMessage());
                    })
                    .doFinally(signalType -> {
                        metrics.stopRequestTimer(stopwatch);

                        int statusCode = (exchange.getResponse().getStatusCode() != null)
                                ? exchange.getResponse().getStatusCode().value() : 200;

                        metrics.recordRoutingStatus(finalRouteId, statusCode);

                        log.info("GATEWAY OUTBOUND: Method={} | Path={} | Service={} | Status={}",
                                request.getMethod(), request.getPath(), finalRouteId, statusCode);
                    });
        };
    }
}
