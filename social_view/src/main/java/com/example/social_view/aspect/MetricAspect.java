package com.example.social_view.aspect;


import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


@RequiredArgsConstructor

@Order(2)
@Component
@Aspect
public class MetricAspect {

    private final MeterRegistry meterRegistry;

    @Around("execution(* com.example.social_view.controller..*(..))")
    public Object getMetric(ProceedingJoinPoint jp)throws Throwable{

        String controller = jp.getSignature().getDeclaringType().getSimpleName();

        String method = jp.getSignature().getName();

        Timer.Sample sample = Timer.start(meterRegistry);

        try{
            Object result = jp.proceed();

            sample.stop(
                    Timer.builder("http.api.latency")
                            .tag("controller",controller)
                            .tag("method",method)
                            .tag("status","success")
                            .publishPercentiles(0.5,0.95,0.99)
                            .publishPercentileHistogram()
                            .register(meterRegistry)
            );

            meterRegistry.counter("http.api.counter",
                    "method",method,
                    "controller",controller,
                    "status","success").increment();

            return result;
        }catch (Exception ex){

            meterRegistry.counter("http.api.counter",
                    "method",method,
                    "controller",controller,
                    "status","error").increment();

            throw ex;

        }

    }
}
