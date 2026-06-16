package com.example.social_interaction.aspect;


import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Aspect
@Order(2)
public class MetricAspect {

    private final MeterRegistry meterRegistry;

    @Around("execution(* com.example.social_interaction.controller..*(..))")
    public Object getMetric(ProceedingJoinPoint jp)throws Throwable{

        String api = jp.getSignature().getName();

        String controller = jp.getSignature().getDeclaringType().getSimpleName();

        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            Object result = jp.proceed();

            sample.stop(
                    Timer.builder("http.api.latency")
                            .tag("method",api)
                            .tag("controller",controller)
                            .tag("Status","Success")
                            .publishPercentiles(0.5,0.99,0.95)
                            .publishPercentileHistogram()
                            .register(meterRegistry)
            );

            meterRegistry.counter("http.api.count",
                    "method",api,
                    "controller",controller,
                    "Status","Success"
            ).increment();

            return result;
        }catch (Exception ex){

            meterRegistry.counter("http.api.count",
                    "method",api,
                    "controller",controller,
                    "Status","Error"
            ).increment();

            throw ex;
        }

    }


}
