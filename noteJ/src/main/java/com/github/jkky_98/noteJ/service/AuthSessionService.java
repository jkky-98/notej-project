package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.web.controller.dto.LoginForm;
import com.github.jkky_98.noteJ.web.controller.dto.SignUpForm;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.domain.user.UserRole;
import com.github.jkky_98.noteJ.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthSessionService implements AuthService {

    private final UserRepository userRepository;

    @Override
    public void logout(HttpSession session) {
        session.invalidate();
        return;
    }

    @Override
    public User login(LoginForm form) {
        String loginId = form.getUsername();
        String loginPassword = form.getPassword();
        return userRepository.findByUsername(loginId).filter(m -> m.getPassword().equals(loginPassword))
                .orElse(null);
    }

    @Override
    public User signUp(SignUpForm signUpForm) {
        User signUpUser = User.builder()
                .username(signUpForm.getUsername())
                .email(signUpForm.getEmail())
                .password(signUpForm.getPassword())
                .userRole(UserRole.USER)
                .build();

        return userRepository.save(signUpUser);
    }
}
