package me.notej.notej_api.security.refresh.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UnsupportedGrantTypeException extends RuntimeException {
    public UnsupportedGrantTypeException(String message) {
        super(message);
    }
}
