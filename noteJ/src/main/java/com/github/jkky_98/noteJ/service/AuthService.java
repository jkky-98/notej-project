package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.web.controller.form.LoginForm;
import com.github.jkky_98.noteJ.web.controller.form.SignUpForm;
import com.github.jkky_98.noteJ.domain.user.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.validation.BindingResult;

// toDo: signUp에서 email 중복 검증 필요
public interface AuthService {
    User login(LoginForm loginForm, BindingResult bindingResult);
    User signUp(SignUpForm signUpForm, BindingResult bindingResult);
    void logout(HttpSession session);
}
