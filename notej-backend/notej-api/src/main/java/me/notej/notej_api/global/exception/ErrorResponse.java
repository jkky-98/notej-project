package me.notej.notej_api.global.exception;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

public record ErrorResponse(
        LocalDateTime timestamp,
        int           status,
        String        error,
        String        path
) {

    public static ErrorResponse of(int status, String error) {
        return new ErrorResponse(
                LocalDateTime.now(),
                status,
                error,
                currentRequestPath()
        );
    }

    private static String currentRequestPath() {
        var attrs = (ServletRequestAttributes)
                RequestContextHolder.getRequestAttributes();
        return (attrs != null)
                ? attrs.getRequest().getRequestURI()
                : "";
    }
}

