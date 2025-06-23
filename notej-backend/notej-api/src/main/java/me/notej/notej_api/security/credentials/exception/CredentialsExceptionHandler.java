package me.notej.notej_api.security.credentials.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import me.notej.notej_api.common.exception.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class CredentialsExceptionHandler {

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmail(DuplicateEmailException ex, HttpServletRequest request) {
        var body = ErrorResponse.of(HttpStatus.CONFLICT.value(), ex.getMessage());
        // 409 Conflict: 이미 존재하는 리소스

        log.warn("[CredentialsExceptionHandler][handleDuplicateEmail] DuplicateEmailException: {} | IP={} | URI={}",
                ex.getMessage(), request.getRemoteAddr(), request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(body);
    }

    @ExceptionHandler(SocialLoginExistsException.class)
    public ResponseEntity<ErrorResponse> handleSocialLoginExists(SocialLoginExistsException ex, HttpServletRequest request) {
        var body = ErrorResponse.of(HttpStatus.CONFLICT.value(), ex.getMessage());
        // 409 Conflict: 소셜 로그인 이력이 있는 이메일

        log.warn("[CredentialsExceptionHandler][handleSocialLoginExists] SocialLoginExistsException: {} | IP={} | URI={}",
                ex.getMessage(), request.getRemoteAddr(), request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException ex, HttpServletRequest request) {
        var body = ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        // 400 Bad Request: 유효하지 않은 요청

        log.warn("[CredentialsExceptionHandler][handleBadRequest] IllegalArgumentException: {} | IP={} | URI={}",
                ex.getMessage(), request.getRemoteAddr(), request.getRequestURI());

        return ResponseEntity
                .badRequest()
                .body(body);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.UNAUTHORIZED.value(),
                ex.getMessage()
        );

        log.warn("[CredentialsExceptionHandler][handleBadCredentialsException] BadCredentialsException: {} | IP={} | URI={}",
                ex.getMessage(), request.getRemoteAddr(), request.getRequestURI());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(errorResponse);
    }
}
