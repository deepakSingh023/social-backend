package com.example.gateway.filter;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class GatewayMetrics {

    private final MeterRegistry registry;

    // ----------------------------
    // Authentication Metrics
    // ----------------------------

    private final Counter authenticatedRequests;

    private final Counter unauthorizedRequests;

    private final Counter jwtValidationFailures;

    // ----------------------------
    // Gateway Metrics
    // ----------------------------

    private final Counter successfulRoutes;

    private final Counter routingFailures;

    // ----------------------------
    // Performance Metrics
    // ----------------------------

    private final Timer requestProcessingTimer;

    public GatewayMetrics(MeterRegistry registry) {

        this.registry = registry;

        authenticatedRequests = Counter.builder("gateway.authenticated.requests")
                .description("Total authenticated requests")
                .register(registry);

        unauthorizedRequests = Counter.builder("gateway.unauthorized.requests")
                .description("Unauthorized requests")
                .register(registry);

        jwtValidationFailures = Counter.builder("gateway.jwt.validation.failures")
                .description("JWT validation failures")
                .register(registry);

        successfulRoutes = Counter.builder("gateway.routing.success")
                .description("Successfully routed requests")
                .register(registry);

        routingFailures = Counter.builder("gateway.routing.failures")
                .description("Failed routed requests")
                .register(registry);

        requestProcessingTimer = Timer.builder("gateway.request.processing.time")
                .description("Gateway request processing time")
                .publishPercentiles(0.50, 0.90, 0.95, 0.99)
                .register(registry);
    }

    // --------------------------------------------------
    // Counter APIs
    // --------------------------------------------------

    public void authenticatedRequest() {
        authenticatedRequests.increment();
    }

    public void unauthorizedRequest() {
        unauthorizedRequests.increment();
    }

    public void jwtValidationFailure() {
        jwtValidationFailures.increment();
    }

    public void successfulRoute() {
        successfulRoutes.increment();
    }

    public void routingFailure() {
        routingFailures.increment();
    }

    // --------------------------------------------------
    // Timer API
    // --------------------------------------------------

    public Timer.Sample startRequestTimer() {
        return Timer.start(registry);
    }

    public void stopRequestTimer(Timer.Sample sample) {
        sample.stop(requestProcessingTimer);
    }
}