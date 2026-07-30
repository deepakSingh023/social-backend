package com.example.social_post.aspects;


import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


@RequiredArgsConstructor
@Component
@Aspect
@Order(2)
public class metricAspect {

    private final MeterRegistry meterRegistry;

    @Around("execution(* com.example.social_post.service..*(..))")
    public Object getMetrics(ProceedingJoinPoint jp) throws Throwable {

        String method = jp.getSignature().getName();
        String controller = jp.getSignature().getDeclaringType().getSimpleName();

        // 1. Start the micrometer stopwatch
        Timer.Sample sample = Timer.start(meterRegistry);
        String status = "Success";

        try {
            return jp.proceed();
        } catch (Exception ex) {
            status = "Error";
            throw ex;
        } finally {

            sample.stop(
                    Timer.builder("http.api.latency")
                            .tag("controller", controller)
                            .tag("method", method)
                            .tag("Status", status)
                            .publishPercentiles(0.5, 0.95, 0.99)
                            .publishPercentileHistogram()
                            .register(meterRegistry)
            );


            meterRegistry.counter("http.api.count",
                    "method", method,
                    "controller", controller,
                    "Status", status
            ).increment();
        }
    }
}
