package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.mapper.UserMapper;
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

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthSessionService implements AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private final UserService userService;

    @Override
    public void logout(HttpSession session) {
        session.invalidate();
    }

    @Override
    @Transactional(noRollbackFor = BadCredentialsException.class)
    public User login(LoginForm form) throws BadCredentialsException {
        String username = form.getUsername();
        String password = form.getPassword();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("존재하지 않는 아이디입니다.", 0));

        checkAccountLock(user);

        if (!user.isPasswordValid(password)) {
            handleInvalidPassword(user);
            throw new BadCredentialsException("비밀번호가 틀렸습니다.", user.getFailedCount());
        }

        user.initFailedCount();
        return user;
    }

    private void checkAccountLock(User user) throws LockedException {
        if (!user.isAccountLocked()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime accountExpiredTime = user.getAccountExpiredTime();

        if (accountExpiredTime.isBefore(now)) {
            // 잠금 기간이 만료되었으면 초기화
            user.initFailedCount();
            user.initAccountLocked();
        } else {
            // 남은 잠금 시간을 계산하여 예외 발생
            Duration duration = Duration.between(now, accountExpiredTime);
            int minutesLeft = (int) Math.ceil(duration.getSeconds() / 60.0);
            throw new LockedException("계정이 잠겼습니다. " + minutesLeft + "분 후에 다시 로그인 하세요.");
        }
    }

    private void handleInvalidPassword(User user) {
        if (user.getFailedCount() >= MAX_FAILED_ATTEMPTS) {
            user.updateAccountLocked();
            user.updateAccountExpiredTime();
        } else {
            user.increaseFailedCount();
        }
    }


    @Override
    public User signUp(SignUpForm signUpForm) {
        // 중복 검증
        validSignUpForm(signUpForm);

        User signUpUser = createUserWithDescription(signUpForm);

        return userService.saveUser(signUpUser);
    }

    /**
     * User, UserDesc 엔티티 생성 메서드(UserDesc는 User에 내재된다.)
     * @param signUpForm
     * @return
     */
    private User createUserWithDescription(SignUpForm signUpForm) {
        UserDesc userDesc = userMapper.toUserDescSignUp(signUpForm);
        User user = userMapper.toUserSignUp(signUpForm, userDesc);

        return user;
    }

    /**
     * 이메일, Username 중복 검증
     * @param signUpForm
     */
    @Transactional(readOnly = true)
    public void validSignUpForm(SignUpForm signUpForm) {
        validateUsernameDuplication(signUpForm.getUsername());
        validateEmailDuplication(signUpForm.getEmail());
    }

    private void validateUsernameDuplication(String username) {
        userRepository.findByUsername(username)
                .ifPresent(user -> { throw new DataIntegrityViolationException("Username already exists."); });
    }

    private void validateEmailDuplication(String email) {
        userRepository.findByEmail(email)
                .ifPresent(user -> { throw new DataIntegrityViolationException("Email already exists."); });
    }
}
