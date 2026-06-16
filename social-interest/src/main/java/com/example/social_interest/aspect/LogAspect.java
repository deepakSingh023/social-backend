package com.example.social_interest.aspect;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


@Aspect
@Component
@Order(3)
public class LogAspect {

    private final static Logger log = LoggerFactory.getLogger(LogAspect.class);

    @Around("execution(* com.example.social_interest.controller..*(..))")
    public Object getLogger(ProceedingJoinPoint jp)throws Throwable{

        long start = System.currentTimeMillis();

        String method = jp.getSignature().getName();

        String controller = jp.getSignature().getDeclaringType().getSimpleName();

        try{

            Object result = jp.proceed();
            long latency = System.currentTimeMillis()-start;

            log.info("controller={}  api={} status=SUCCESS  latencyMs = {}",controller,method,latency);

            return result;

        }catch (Exception ex){

            long latency =  System.currentTimeMillis()-start;

            log.error("controller={}  api={} status=SUCCESS  latencyMs = {}",controller,method,latency);

            throw ex;
        }
    }
}
