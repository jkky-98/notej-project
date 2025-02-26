package com.github.jkky_98.noteJ.exception.handler;

import com.github.jkky_98.noteJ.exception.UnauthenticatedUserException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class LoginExceptionHandler {

    @ExceptionHandler(UnauthenticatedUserException.class)
    public String handleUnauthenticatedUserException(UnauthenticatedUserException e) {
        return "redirect:/login";
    }
}
