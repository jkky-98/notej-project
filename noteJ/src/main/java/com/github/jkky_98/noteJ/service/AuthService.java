package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.web.controller.form.LoginForm;
import com.github.jkky_98.noteJ.web.controller.form.SignUpForm;
import com.github.jkky_98.noteJ.domain.user.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.validation.BindingResult;

import javax.naming.AuthenticationException;

public interface AuthService {
    User login(LoginForm loginForm) throws AuthenticationException;
    User signUp(SignUpForm signUpForm);
    void logout(HttpSession session);
}
