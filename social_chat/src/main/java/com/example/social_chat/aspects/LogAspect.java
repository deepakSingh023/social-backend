package com.example.social_chat.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(3)
@Aspect
public class LogAspect {

    private static final Logger log = LoggerFactory.getLogger(LogAspect.class);

    @Around("execution(* com.example.social_chat.controller..*(..))")
    private Object getLogging(ProceedingJoinPoint jp)throws Throwable{

        String controller = jp.getSignature().getDeclaringType().getSimpleName();

        String method = jp.getSignature().getName();

        Long start = System.currentTimeMillis();

        try {

            Object result = jp.proceed();

            Long latency = System.currentTimeMillis()-start;

            log.info("api={} method={} status=SUCCESS latencyMs={}",controller,method,latency);

            return result;

        }catch (Exception ex){

            Long latency = System.currentTimeMillis() - start;

            log.error("api={} method={} status=SUCCESS latencyMs={}",controller,method,latency);

            throw ex;
        }
    }
}