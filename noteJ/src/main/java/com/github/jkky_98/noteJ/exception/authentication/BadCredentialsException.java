package com.github.jkky_98.noteJ.exception.authentication;

import lombok.Getter;

@Getter
public class BadCredentialsException extends RuntimeException {

    private final int failedCount;

    public BadCredentialsException(int failedCount) {
        super();
        this.failedCount = failedCount;
    }

    public BadCredentialsException(String message, int failedCount) {
        super(message);
        this.failedCount = failedCount;
    }

    public BadCredentialsException(String message, Throwable cause, int failedCount) {
        super(message, cause);
        this.failedCount = failedCount;
    }

    public BadCredentialsException(Throwable cause, int failedCount) {
        super(cause);
        this.failedCount = failedCount;
    }

    protected BadCredentialsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, int failedCount) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.failedCount = failedCount;
    }
}
