package com.franklintju.streamlab.common;

import com.franklintju.streamlab.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class DistributedLockAspect {

    private final RedisLockService redisLockService;

    private static final String LOCK_PREFIX = "lock:";

    @Around("@annotation(com.franklintju.streamlab.common.DistributedLock)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        DistributedLock annotation = signature.getMethod().getAnnotation(DistributedLock.class);

        String key = LOCK_PREFIX + resolveKey(annotation.key(), joinPoint);
        String lockValue = redisLockService.acquireLock(key, annotation.expireSeconds());

        if (lockValue == null) {
            log.warn("获取分布式锁失败: key={}, message={}", key, annotation.message());
            throw new BusinessException(annotation.message(), HttpStatus.SERVICE_UNAVAILABLE);
        }

        try {
            return joinPoint.proceed();
        } finally {
            boolean released = redisLockService.releaseLock(key, lockValue);
            if (!released) {
                log.warn("释放分布式锁失败: key={}", key);
            }
        }
    }

    private String resolveKey(String keyExpression, ProceedingJoinPoint joinPoint) {
        if (keyExpression == null || keyExpression.isEmpty()) {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            return signature.getDeclaringType().getSimpleName() + ":" + signature.getName();
        }
        if (!keyExpression.contains("#")) {
            return keyExpression;
        }
        Object[] args = joinPoint.getArgs();
        String[] paramNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
        if (paramNames == null) {
            return keyExpression;
        }
        for (int i = 0; i < paramNames.length; i++) {
            String placeholder = "#" + paramNames[i];
            if (keyExpression.contains(placeholder)) {
                keyExpression = keyExpression.replace(placeholder, String.valueOf(args[i]));
            }
        }
        return keyExpression;
    }
}
