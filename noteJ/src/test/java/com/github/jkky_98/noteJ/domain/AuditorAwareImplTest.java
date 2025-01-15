package com.github.jkky_98.noteJ.domain;

import com.github.jkky_98.noteJ.domain.base.AuditorAwareImpl;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.web.session.SessionConst;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AuditorAwareImplTest {

    private AuditorAwareImpl auditorAware;
    private HttpSession httpSession;

    @BeforeEach
    void setup() {
        httpSession = new MockHttpSession();
        auditorAware = new AuditorAwareImpl(httpSession);
    }

    @Test
    @DisplayName("[AuditorAwareImpl] 로그인된 사용자 반환 테스트")
    void getCurrentAuditorWhenUserIsLoggedInTest() {
        // given
        User mockUser = User.builder().username("testuser").build();
        httpSession.setAttribute(SessionConst.LOGIN_USER, mockUser);

        // when
        Optional<String> auditor = auditorAware.getCurrentAuditor();

        // then
        assertThat(auditor).isPresent();
        assertThat(auditor.get()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("[AuditorAwareImpl] 로그인되지 않은 사용자 반환 테스트")
    void getCurrentAuditorWhenUserIsNotLoggedInTest() {
        // when
        Optional<String> auditor = auditorAware.getCurrentAuditor();

        // then
        assertThat(auditor).isPresent();
        assertThat(auditor.get()).isEqualTo("anonymous");
    }
}
