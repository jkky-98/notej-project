package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.FileMetadata;
import com.github.jkky_98.noteJ.domain.user.ThemeMode;
import com.github.jkky_98.noteJ.domain.user.UserDesc;
import com.github.jkky_98.noteJ.web.controller.form.LoginForm;
import com.github.jkky_98.noteJ.web.controller.form.SignUpForm;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.domain.user.UserRole;
import com.github.jkky_98.noteJ.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

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
    public User signUp(SignUpForm signUpForm, BindingResult bindingResult) {
        // 중복 체크: username
        if (userRepository.findByUsername(signUpForm.getUsername()).isPresent()) {
            bindingResult.rejectValue("username", "error.username", "Username already exists.");
        }

        // 중복 체크: email
        if (userRepository.findByEmail(signUpForm.getEmail()).isPresent()) {
            bindingResult.rejectValue("email", "error.email", "Email already exists.");
        }

        // 중복이 있으면 바로 반환하여 처리를 중단
        if (bindingResult.hasErrors()) {
            return null;
        }
        FileMetadata fileMetadata = FileMetadata.builder()
                .build();

        UserDesc userDesc = UserDesc.builder()
                .blogTitle(signUpForm.getBlogTitle())
                .socialEmail(signUpForm.getEmail())
                .commentAlarm(true)
                .noteJAlarm(true)
                .theme(ThemeMode.LIGHT)
                .profilePic("img/default-profile.png")
                .fileMetadata(fileMetadata)
                .build();

        User signUpUser = User.builder()
                .username(signUpForm.getUsername())
                .email(signUpForm.getEmail())
                .password(signUpForm.getPassword())
                .userRole(UserRole.USER)
                .userDesc(userDesc)
                .build();

        return userRepository.save(signUpUser);
    }
}
