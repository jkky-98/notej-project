package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.repository.UserRepository;
import com.github.jkky_98.noteJ.web.controller.form.LoginForm;
import com.github.jkky_98.noteJ.web.controller.form.SignUpForm;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import javax.naming.AuthenticationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthSessionServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthSessionService authSessionService;

    @Test
    @DisplayName("[AuthSessionService] logout() 호출 시 세션 무효화 Mock Test")
     void logoutTest() {
        HttpSession session = mock(HttpSession.class);
        authSessionService.logout(session);
        verify(session, times(1)).invalidate();
    }

    @Test
    @DisplayName("[AuthSessionService] login() 성공 케이스: 올바른 아이디와 비밀번호")
    void loginSuccessTest() throws AuthenticationException {
        // given
        LoginForm form = new LoginForm();
        form.setUsername("testuser");
        form.setPassword("password123");

        User testUser = User.builder()
                .username("testuser")
                .password("password123")
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // when
        User result = authSessionService.login(form);

        // then
        assertThat(result).isEqualTo(testUser);
    }

    @Test
    @DisplayName("[AuthSessionService] login() 실패 케이스: 잘못된 비밀번호")
    void loginFailTest() {
        // given
        LoginForm form = new LoginForm();
        form.setUsername("testuser");
        form.setPassword("wrongpassword");

        User testUser = User.builder()
                .username("testuser")
                .password("password123")
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // when & then
        assertThrows(AuthenticationException.class, () -> authSessionService.login(form));
    }

    @Test
    @DisplayName("[AuthSessionService] signUp() 성공 케이스: 중복 없음")
    void signUpSuccessTest() {
        // given
        SignUpForm form = new SignUpForm();
        form.setUsername("newuser");
        form.setPassword("password123");
        form.setEmail("newuser@example.com");

        // 중복 검증을 위해 리포지토리가 빈 Optional 반환
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.empty());
        // 저장 시 리포지토리가 전달받은 User 객체를 반환하도록 설정
        User newUser = User.builder()
                        .username("newuser")
                        .password("password123")
                        .email("newuser@example.com")
                        .build();

        when(userRepository.save(any(User.class))).thenReturn(newUser);

        // when
        User result = authSessionService.signUp(form);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("newuser");
        assertThat(result.getEmail()).isEqualTo("newuser@example.com");
    }

    @Test
    @DisplayName("[AuthSessionService] signUp() 실패 케이스: username 중복")
    void signUpUsernameDuplicationTest() {
        // given
        SignUpForm form = new SignUpForm();
        form.setUsername("newuser");
        form.setPassword("password123");
        form.setEmail("newuser@example.com");

        // username 중복 발생
        User newUser = User.builder()
                .username("newuser")
                .password("password123")
                .email("newuser@example.com")
                .build();

        when(userRepository.findByUsername("newuser")).thenReturn(Optional.of(newUser));

        // when & then
        assertThrows(DataIntegrityViolationException.class, () -> authSessionService.signUp(form));
    }

    @Test
    @DisplayName("[AuthSessionService] signUp() 실패 케이스: email 중복")
    void signUpEmailDuplicationTest() {
        // given
        SignUpForm form = new SignUpForm();
        form.setUsername("newuser");
        form.setPassword("password123");
        form.setEmail("test@example.com");
        // email 중복 발생
        User oldUser = User.builder()
                .username("olduser")
                .password("password123")
                .email("test@example.com")
                .build();
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(oldUser));

        // when & then
        assertThrows(DataIntegrityViolationException.class, () -> authSessionService.signUp(form));
    }
}
