package com.example.social_post.aspects;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Aspect
@Order(3)
public class loggingAspect {

    public static final Logger log = LoggerFactory.getLogger(loggingAspect.class);


    @Around("execution(* com.example.social_post.controller..*(..))")
    public Object logApi(ProceedingJoinPoint jp) throws Throwable {

        long start = System.currentTimeMillis();

        String controller = jp.getSignature().getDeclaringType().getSimpleName();

        String method = jp.getSignature().getName();

        try{
            Object result = jp.proceed();

            long latency = System.currentTimeMillis() - start;

            log.info("controller={}  api={} status=SUCCESS  latencyMs = {}",controller,method,latency);
            return result;
        }catch(Exception ex){
            long latency = System.currentTimeMillis() - start;

            log.error("controller={} api={} status=ERROR latencyMs={} error={}",
                    controller, method, latency, ex.getMessage());

            throw ex;

        }

    }
}
