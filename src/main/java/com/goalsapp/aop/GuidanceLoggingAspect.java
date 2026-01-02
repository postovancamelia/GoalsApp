package com.goalsapp.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class GuidanceLoggingAspect {

    @Around("execution(* com.goalsapp.service.GuidanceService.getGuidance(..))")
    public Object logGuidance(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            System.out.println("[AOP] " + pjp.getSignature() + " called");
            return pjp.proceed();
        } finally {
            System.out.println("[AOP] took " + (System.currentTimeMillis() - start) + " ms");
        }
    }
}
