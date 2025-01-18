package com.github.jkky_98.noteJ.exception;

import org.springframework.http.HttpStatus;

public interface BusinessException {
    String getErrorCode();
    HttpStatus getHttpStatus();
}
