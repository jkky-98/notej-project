package com.github.jkky_98.noteJ.exception.handler;

import com.github.jkky_98.noteJ.exception.SelfLikeException;
import com.github.jkky_98.noteJ.exception.UnauthenticatedUserException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UnauthenticatedUserException.class)
    public String handleUnauthenticatedUserException(UnauthenticatedUserException e) {
        return "redirect:/login";
    }

    @ExceptionHandler(SelfLikeException.class)
    public ResponseEntity<String> handleSelfLikeException(SelfLikeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST) // 400 Bad Request
                .body(ex.getMessage()); // 에러 메시지 반환
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ModelAndView handleNoResourceFoundException(NoResourceFoundException ex) {
        ModelAndView modelAndView = new ModelAndView("error/no-resource");
        return modelAndView;
    }
}
