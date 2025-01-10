package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.user.ThemeMode;
import com.github.jkky_98.noteJ.domain.user.UserDesc;
import com.github.jkky_98.noteJ.web.controller.form.LoginForm;
import com.github.jkky_98.noteJ.web.controller.form.SignUpForm;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.domain.user.UserRole;
import com.github.jkky_98.noteJ.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;

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
    public User login(LoginForm form) throws AuthenticationException {
        String loginId = form.getUsername();
        String loginPassword = form.getPassword();

        User loginUser = userRepository.findByUsername(loginId)
                .filter(user -> user.getPassword().equals(loginPassword))
                .orElse(null);

        if (loginUser == null) {
            throw new AuthenticationException("아이디 또는 비밀번호가 맞지 않습니다.");
        }

        return loginUser;
    }

    @Override
    public User signUp(SignUpForm signUpForm) {
        // 중복 검증
        validSignUpForm(signUpForm);

        /**
         * User, UserDesc 구축
         */
        User signUpUser = initializeUser(signUpForm);

        return userRepository.save(signUpUser);
    }

    private static User initializeUser(SignUpForm signUpForm) {
        UserDesc userDesc = UserDesc.builder()
                .blogTitle(signUpForm.getBlogTitle())
                .socialEmail(signUpForm.getEmail())
                .commentAlarm(true)
                .noteJAlarm(true)
                .theme(ThemeMode.LIGHT)
                .build();

        User signUpUser = User.builder()
                .username(signUpForm.getUsername())
                .email(signUpForm.getEmail())
                .password(signUpForm.getPassword())
                .userRole(UserRole.USER)
                .userDesc(userDesc)
                .build();
        return signUpUser;
    }

    private void validSignUpForm(SignUpForm signUpForm) {
        // 중복 체크: username
        if (userRepository.findByUsername(signUpForm.getUsername()).isPresent()) {
            throw new DataIntegrityViolationException("Username already exists.");
        }

        // 중복 체크: email
        if (userRepository.findByEmail(signUpForm.getEmail()).isPresent()) {
            throw new DataIntegrityViolationException("Email already exists.");
        }
    }
}
