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


    @Around("execution(* com.example.social_likes.controller..*(..))")
    public Object getMetrics(ProceedingJoinPoint jp)throws Throwable{

        String controller = jp.getSignature().getDeclaringType().getSimpleName();

        String method = jp.getSignature().getName();
        Timer.Sample sample = Timer.start(metricRegistry);


        try{

            Object result = jp.proceed();

            sample.stop(
                    Timer.builder("http.api.latency")
                            .tag("controller",controller)
                            .tag("method",method)
                            .tag("status","success")
                            .publishPercentiles(0.5,0.99,0.95)
                            .publishPercentileHistogram()
                            .register(metricRegistry)

            );

            metricRegistry.counter(
                    "http.api.count",
                    "controller",controller,
                    "method",method,
                    "status","success"
            ).increment();

            return result;

        }catch(Exception ex){

            metricRegistry.counter(
                    "http.api.count",
                    "controller",controller,
                    "method",method,
                    "status","error"
            ).increment();

            throw ex;


        }



    }




}
