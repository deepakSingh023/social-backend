package com.example.Social.profile.aspect;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.UUID;


@Component
@Aspect
@Order(1)
public class TracingAspect {


    @Around("execution(* com.example.Social.profile.controller..*(..))")
    public Object getTracing(ProceedingJoinPoint jp) throws Throwable {

        String traceId = UUID.randomUUID().toString();

        MDC.put("traceId",traceId);

        try{
            return jp.proceed();
        }finally {
            MDC.clear();
        }

    }
}
