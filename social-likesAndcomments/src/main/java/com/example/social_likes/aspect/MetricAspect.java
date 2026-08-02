package com.example.social_likes.aspect;
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
public class MetricAspect {

    public final MeterRegistry metricRegistry;


    @Around("execution(* com.example.social_likes.service..*(..))")
    public Object getMetrics(ProceedingJoinPoint jp) throws Throwable {

        String controller = jp.getSignature().getDeclaringType().getSimpleName();
        String method = jp.getSignature().getName();

        // Start stopwatch using Micrometer's standard clock
        Timer.Sample sample = Timer.start(metricRegistry);
        String status = "success";

        try {
            return jp.proceed();
        } catch (Throwable ex) { // Catch Throwable to match Spring AOP proceed signature
            status = "error";
            throw ex;
        } finally {
            // This blocks guarantees the timer stops even if things crash!
            sample.stop(
                    Timer.builder("http.api.latency")
                            .tag("controller", controller)
                            .tag("method", method)
                            .tag("status", status)
                            .publishPercentiles(0.5, 0.95, 0.99)
                            .publishPercentileHistogram()
                            .register(metricRegistry)
            );

            metricRegistry.counter(
                    "http.api.count",
                    "controller", controller,
                    "method", method,
                    "status", status
            ).increment();
        }
    }
}
