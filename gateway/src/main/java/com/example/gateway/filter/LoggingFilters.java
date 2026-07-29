package com.example.gateway.filter;

import com.example.gateway.filter.GatewayMetrics;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.server.mvc.common.MvcUtils;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.net.URI;

public final class LoggingFilters {

    private static final Logger log = LoggerFactory.getLogger(LoggingFilters.class);

    private LoggingFilters() {}

    public static HandlerFilterFunction<ServerResponse, ServerResponse> audit(GatewayMetrics metrics) {
        return (request, next) -> {

            // 1. FIXED: Get the Route ID directly from the correct MVC attribute string
            String routeId = (String) request.attributes().get(MvcUtils.GATEWAY_ROUTE_ID_ATTR);
            if (routeId == null) {
                routeId = "unknown-route";
            }

            // 2. FIXED: Get the Target Microservice Downstream Destination URL
            URI targetUriObj = (URI) request.attributes().get(MvcUtils.GATEWAY_REQUEST_URL_ATTR);
            String targetUri = (targetUriObj != null) ? targetUriObj.toString() : "unknown-uri";

            // 3. Start the processing latency stopwatch
            Timer.Sample stopwatch = metrics.startRequestTimer();

            log.info("GATEWAY INBOUND: Method={} | Path={} | TargetedService={} | TargetURI={}",
                    request.method(), request.path(), routeId, targetUri);

            ServerResponse response;
            try {
                // Execute the downstream network call to the microservice
                response = next.handle(request);
            } catch (Exception ex) {
                metrics.recordRoutingStatus(routeId, 500);
                log.error("GATEWAY ERROR: Request failed routing to service={}. Error={}", routeId, ex.getMessage());
                throw ex;
            } finally {
                metrics.stopRequestTimer(stopwatch);
            }

            // Increment metrics with explicit status code dimensions
            metrics.recordRoutingStatus(routeId, response.statusCode().value());

            // Log the final exit state in a clean, unified structure
            log.info("GATEWAY OUTBOUND: Method={} | Path={} | Service={} | Status={}",
                    request.method(), request.path(), routeId, response.statusCode().value());

            return response;
        };
    }
}

