package com.github.jkky_98.noteJ.exception.handler;

import com.github.jkky_98.noteJ.exception.SelfLikeException;
import com.github.jkky_98.noteJ.exception.UnauthenticatedUserException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(SelfLikeException.class)
    public ResponseEntity<String> handleSelfLikeException(SelfLikeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST) // 400 Bad Request
                .body(ex.getMessage()); // 에러 메시지 반환
    }

    @ExceptionHandler({NoResourceFoundException.class, EntityNotFoundException.class})
    public ModelAndView handleNoResourceFoundException(Exception ex) {
        log.error(ex.getMessage(), ex);
        return new ModelAndView("error/error404");
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleAllExceptions(Exception ex) {
        log.error(ex.getMessage(), ex);
        return new ModelAndView("error/error500");
    }
}
