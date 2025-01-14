package com.github.jkky_98.noteJ.exception.handler;

import com.github.jkky_98.noteJ.exception.UnauthenticatedUserException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UnauthenticatedUserException.class)
    public String handleUnauthenticatedUserException(UnauthenticatedUserException e) {
        return "redirect:/login";
    }
}
