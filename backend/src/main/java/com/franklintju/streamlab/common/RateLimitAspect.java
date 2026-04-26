package com.franklintju.streamlab.common;

import com.franklintju.streamlab.exceptions.RateLimitExceededException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final RateLimiterService rateLimiterService;

    @Around("@annotation(com.franklintju.streamlab.common.RateLimit)")
    public Object rateLimit(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);

        String key = resolveKey(rateLimit.key(), joinPoint);
        boolean allowed = rateLimiterService.isAllowed(
                key,
                rateLimit.maxRequests(),
                rateLimit.windowSeconds()
        );

        if (!allowed) {
            throw new RateLimitExceededException(rateLimit.message());
        }

        return joinPoint.proceed();
    }

    private String resolveKey(String keyExpression, ProceedingJoinPoint joinPoint) {
        if (keyExpression == null || keyExpression.isEmpty()) {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            return signature.getName();
        }

        if (keyExpression.contains("#")) {
            Object[] args = joinPoint.getArgs();
            String[] paramNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();

            for (int i = 0; i < paramNames.length; i++) {
                String placeholder = "#" + paramNames[i];
                if (keyExpression.contains(placeholder)) {
                    keyExpression = keyExpression.replace(placeholder, String.valueOf(args[i]));
                }
            }
        }

        return keyExpression;
    }
}
