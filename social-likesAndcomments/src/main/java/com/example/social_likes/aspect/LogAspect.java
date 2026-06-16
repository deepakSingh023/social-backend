package com.example.social_likes.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


@Component
@Aspect
@Order(3)
public class LogAspect {

    private final static Logger log = LoggerFactory.getLogger(LogAspect.class);


    @Around("execution(* com.example.social_likes.controller..*(..))")
    public Object getLog(ProceedingJoinPoint jp) throws Throwable{

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