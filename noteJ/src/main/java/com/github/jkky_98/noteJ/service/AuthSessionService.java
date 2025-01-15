package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.user.UserDesc;
import com.github.jkky_98.noteJ.web.controller.form.LoginForm;
import com.github.jkky_98.noteJ.web.controller.form.SignUpForm;
import com.github.jkky_98.noteJ.domain.user.User;
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
    }

    @Override
    public User login(LoginForm form) throws AuthenticationException {
        String AuthenticationUsername = form.getUsername();
        String AuthenticationPassword = form.getPassword();

        return userRepository.findByUsername(AuthenticationUsername)
                .filter(user -> user.isPasswordValid(AuthenticationPassword))
                .orElseThrow(() -> new AuthenticationException("아이디 또는 비밀번호가 맞지 않습니다."));
    }

    @Override
    public User signUp(SignUpForm signUpForm) {
        // 중복 검증
        validSignUpForm(signUpForm);

        User signUpUser = createUserWithDescription(signUpForm);

        return userRepository.save(signUpUser);
    }

    /**
     * User, UserDesc 엔티티 생성 메서드(UserDesc는 User에 내재된다.)
     * @param signUpForm
     * @return
     */
    private static User createUserWithDescription(SignUpForm signUpForm) {
        UserDesc userDesc = UserDesc.of(signUpForm);
        User signUpUser = User.of(signUpForm, userDesc);
        return signUpUser;
    }

    /**
     * 이메일, Username 중복 검증
     * @param signUpForm
     */
    private void validSignUpForm(SignUpForm signUpForm) {
        validateUsernameDuplication(signUpForm.getUsername());
        validateEmailDuplication(signUpForm.getEmail());
    }

    private void validateUsernameDuplication(String username) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new DataIntegrityViolationException("Username already exists.");
        }
    }

    private void validateEmailDuplication(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new DataIntegrityViolationException("Email already exists.");
        }
    }
}
