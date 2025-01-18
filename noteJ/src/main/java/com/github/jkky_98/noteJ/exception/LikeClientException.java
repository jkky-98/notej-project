package com.github.jkky_98.noteJ.exception;

import org.springframework.http.HttpStatus;

public class LikeClientException extends RuntimeException implements BusinessException {

    private final String errorCode;

    public LikeClientException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
