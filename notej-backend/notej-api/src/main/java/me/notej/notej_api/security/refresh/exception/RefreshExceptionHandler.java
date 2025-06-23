package me.notej.notej_api.security.refresh.exception;

import lombok.extern.slf4j.Slf4j;
import me.notej.notej_api.common.exception.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class RefreshExceptionHandler {
    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRefresh(InvalidRefreshTokenException ex) {
        var body = ErrorResponse.of(HttpStatus.UNAUTHORIZED.value(), ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(body);
    }

    @ExceptionHandler(UnsupportedGrantTypeException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedGrant(UnsupportedGrantTypeException ex) {
        var body = ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(body);
    }
}
