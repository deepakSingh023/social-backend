package com.example.social_post.aspects;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;


@Component
@Aspect
@Order(1)
public class tracingAspect {

    @Around("execution(* com.example.social_post.controller..*(..))")
    public Object getTracing(ProceedingJoinPoint jp) throws Throwable, IOException{

        String traceId = UUID.randomUUID().toString();

        MDC.put("traceId",traceId);

        try{
           return jp.proceed();
        }finally {
            MDC.clear();

        }

    }
}
