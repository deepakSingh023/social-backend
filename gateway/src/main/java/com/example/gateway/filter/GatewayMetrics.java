package com.example.gateway.filter;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class GatewayMetrics {

    private final MeterRegistry registry;
    private final Timer requestProcessingTimer;

    public GatewayMetrics(MeterRegistry registry) {
        this.registry = registry;

        this.requestProcessingTimer = Timer.builder("gateway.request.processing.time")
                .description("Gateway request processing time distributed by route")
                .publishPercentiles(0.50, 0.90, 0.95, 0.99)
                .register(registry);
    }

    // Dynamic Counter using tags for auth states
    public void incrementAuthRequest(boolean isAuthenticated) {
        Counter.builder("gateway.auth.requests")
                .description("Total authentication traffic status")
                .tag("status", isAuthenticated ? "authenticated" : "unauthorized")
                .register(registry)
                .increment();
    }

    // Dynamic Counter for JWT errors
    public void jwtValidationFailure(String cause) {
        Counter.builder("gateway.jwt.validation.failures")
                .description("JWT validation failure categorised by root cause")
                .tag("cause", cause) // e.g., "expired", "invalid_signature", "malformed"
                .register(registry)
                .increment();
    }

    // Dynamic Counter for routing status
    public void recordRoutingStatus(String routeId, int statusCode) {
        String outcome = (statusCode >= 200 && statusCode < 400) ? "SUCCESS" : "FAILURE";

        Counter.builder("gateway.routing.requests")
                .description("Routed request volume by target service and response status")
                .tag("route_id", routeId)         // e.g., "post-service"
                .tag("status_code", String.valueOf(statusCode)) // e.g., "429", "500"
                .tag("outcome", outcome)           // "SUCCESS" or "FAILURE"
                .register(registry)
                .increment();
    }

    public Timer.Sample startRequestTimer() {
        return Timer.start(registry);
    }

    public void stopRequestTimer(Timer.Sample sample) {
        sample.stop(requestProcessingTimer);
    }
}
