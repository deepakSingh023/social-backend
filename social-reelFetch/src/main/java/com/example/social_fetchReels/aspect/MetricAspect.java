package com.example.social_fetchReels.aspect;


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
@Order(2)
@Aspect
public class MetricAspect {

    private final MeterRegistry meterRegistry;

    @Around("execution(* com.example.social_fetchReels.controller..*(..))")
    public Object getMetric(ProceedingJoinPoint jp)throws Throwable{

        String method = jp.getSignature().getName();

        String api = jp.getSignature().getDeclaringType().getSimpleName();

        Timer.Sample sample = Timer.start(meterRegistry);


        try {

            Object result = jp.proceed();
            sample.stop(
                    Timer.builder("http.api.latency")
                            .tag("api",api)
                            .tag("method",method)
                            .tag("status","success")
                            .publishPercentiles(0.5,0.95,0.99)
                            .publishPercentileHistogram()
                            .register(meterRegistry)
            );

            meterRegistry.counter("http.api.counter",
                    "api",api,
                    "method",method,
                    "status","success").increment();


            return result;

        }catch (Exception ex){

            meterRegistry.counter("http.api.counter",
                    "api",api,
                    "method",method,
                    "status","error").increment();


            throw ex;

        }

    }
}
