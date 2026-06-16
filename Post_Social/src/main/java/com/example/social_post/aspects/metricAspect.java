package com.example.social_post.aspects;


import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;


@RequiredArgsConstructor
@Component
@Aspect
@Order(2)
public class metricAspect {

    private final MeterRegistry meterRegistry;

    @Around("execution(* com.example.social_post.controller..*(..))")
    public Object getMetrics(ProceedingJoinPoint jp) throws Throwable, IOException{

        String method = jp.getSignature().getName();
        String controller = jp.getSignature().getDeclaringType().getSimpleName();

        Timer.Sample sample = Timer.start(meterRegistry);

        try{

            Object result = jp.proceed();

            sample.stop(
                    Timer.builder("http.api.latency")
                            .tag("controller",controller)
                            .tag("method",method)
                            .tag("Status","Success")
                            .publishPercentiles(0.5,0.99,0.95)
                            .publishPercentileHistogram()
                            .register(meterRegistry)
            );

            meterRegistry.counter("http.api.count",
                    "method",method,
                    "controller",controller,
                    "Status","Success"
            ).increment();

            return result;

        }catch(Exception ex){


            meterRegistry.counter("http.api.count",
                    "method",method,
                    "controller",controller,
                    "Status","Error"
            ).increment();

            throw ex;
        }
    }


}
