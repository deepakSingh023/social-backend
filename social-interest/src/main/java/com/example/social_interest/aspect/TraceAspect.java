package com.example.social_interest.aspect;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.UUID;


@Order(1)
@Component
@Aspect
public class TraceAspect {



    @Around("execution(* com.example.social_interest.controller..*(..))")
    public Object getTracing(ProceedingJoinPoint jp)throws  Throwable{

        String traceId = UUID.randomUUID().toString();

        MDC.put("traceId",traceId);

        try{
            return jp.proceed();
        }finally {
            MDC.clear();
        }

    }
}
