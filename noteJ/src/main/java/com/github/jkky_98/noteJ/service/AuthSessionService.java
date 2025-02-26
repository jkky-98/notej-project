package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.user.UserDesc;
import com.github.jkky_98.noteJ.exception.authentication.BadCredentialsException;
import com.github.jkky_98.noteJ.exception.authentication.LockedException;
import com.github.jkky_98.noteJ.web.controller.form.LoginForm;
import com.github.jkky_98.noteJ.web.controller.form.SignUpForm;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.AuthenticationException;
import javax.security.auth.login.AccountLockedException;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthSessionService implements AuthService {

    private final UserRepository userRepository;

    @Override
    public void logout(HttpSession session) {
        session.invalidate();
    }

    @Override
    @Transactional(noRollbackFor = BadCredentialsException.class)
    public User login(LoginForm form) throws BadCredentialsException {
        String AuthenticationUsername = form.getUsername();
        String AuthenticationPassword = form.getPassword();

        User user = userRepository.findByUsername(AuthenticationUsername)
                .orElseThrow(() -> new BadCredentialsException("존재하지 않는 아이디입니다.", 0));

        // 계정 잠금 확인
        if (user.isAccountLocked()) {
            // 잠금 만료시간 확인
            if (user.getAccountExpiredTime().isBefore(LocalDateTime.now())) {
                // 만료시간이 다 되었을 경우
                user.initFailedCount();
                user.initAccountLocked();
            } else {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime accountExpiredTime = user.getAccountExpiredTime();
                // 몇분 남았는지
                Duration duration = Duration.between(now, accountExpiredTime);
                long seconds = duration.getSeconds();
                int minutesLeft = (int) Math.ceil(seconds / 60.0);

                throw new LockedException("계정이 잠겼습니다. " + minutesLeft + "분 후에 다시 로그인 하세요.");
            }
        }

        if (!user.isPasswordValid(AuthenticationPassword)) {
            int failedCount = user.getFailedCount();

            if (failedCount == 5) {
                user.updateAccountLocked();
                user.updateAccountExpiredTime();
            } else {
                user.increaseFailedCount();
            }

            throw new BadCredentialsException("비밀번호가 틀렸습니다.", user.getFailedCount());
        }

        user.initFailedCount();
        return user;
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
