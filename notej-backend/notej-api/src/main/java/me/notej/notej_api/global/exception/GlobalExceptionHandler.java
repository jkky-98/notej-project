package me.notej.notej_api.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex) {
        log.error("[GlobalExceptionHandler] 예외 발생", ex);
        var body = ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage());
        // 잡지않은 에러의 경우 모두 500 리턴
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
        log.error("[GlobalExceptionHandler] 예외 발생", ex);
        var body = ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(body);
    }
}
