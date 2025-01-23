package com.github.jkky_98.noteJ.aop;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    int maxRequests() default 10; // 허용 요청 수
    int timeWindow() default 60;  // 제한 시간 (초 단위)
}
