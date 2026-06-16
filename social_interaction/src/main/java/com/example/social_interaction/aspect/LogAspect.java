package com.example.social_interaction.aspect;


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

    private static final Logger log = LoggerFactory.getLogger(LogAspect.class);


    @Around("execution(* com.example.social_interaction.controller..*(..))")
    public Object getLogging(ProceedingJoinPoint jp) throws Throwable {

        long start = System.currentTimeMillis();

        String api = jp.getSignature().getName();

        String controller = jp.getSignature().getDeclaringType().getSimpleName();

        try{
            Object result = jp.proceed();

            long latency = System.currentTimeMillis() - start;

            log.info("controller={}  api={} status=SUCCESS  latencyMs = {}",controller,api,latency);
            return result;

        }catch(Exception ex){
            long latency = System.currentTimeMillis() - start;

            log.error("controller={} api={} status=ERROR latencyMs={} error={}",
                    controller, api, latency, ex.getMessage());

            throw ex;
        }

    }
}
