package com.github.jkky_98.noteJ.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Slf4j
@Aspect
public class LogTraceAspect {

    private final LogTrace logTrace;

    public LogTraceAspect(LogTrace logTrace) {
        this.logTrace = logTrace;
    }

    @Pointcut(
            "(" +
            "execution(* com.github.jkky_98.noteJ.service..*(..)) || " +
            "execution(* com.github.jkky_98.noteJ.repository..*(..)) || " +
            "execution(* com.github.jkky_98.noteJ.web..*(..)) || " +
            "execution(* com.github.jkky_98.noteJ.file..*(..)) || " +
            "execution(* com.github.jkky_98.noteJ.exception..*(..)) || " +
            "execution(* com.github.jkky_98.noteJ.domain..*(..)) || " +
            "execution(* com.github.jkky_98.noteJ.deploy..*(..)) " +
            ") && !execution(* com.github.jkky_98.noteJ.web.controller.global..*(..))"
    )
    public void applicationPackagePointcut() {}

    @Around("applicationPackagePointcut()")
    public Object execute(ProceedingJoinPoint joinPoint) throws Throwable {

        // 애플리케이션이 아직 준비되지 않았다면 AOP 적용 안 함
        if (!ApplicationStartupListener.isReady()) {
            return joinPoint.proceed();
        }

        TraceStatus status = null;
        try {
            String message = joinPoint.getSignature().toShortString();
            status = logTrace.begin(message);
            //target 호출
            Object result = joinPoint.proceed();
            logTrace.end(status);
            return result;
        } catch (Exception e) {
            logTrace.exception(status, e);
            throw e;
        }
    }
}