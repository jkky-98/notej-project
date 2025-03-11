package com.github.jkky_98.noteJ.domain.base;

import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.web.session.SessionConst;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Profile({"green", "blue"})
public class AuditorAwareImpl implements AuditorAware<String> {

    private final HttpSession httpSession;

    public AuditorAwareImpl(HttpSession httpSession) {
        this.httpSession = httpSession;
    }

    @Override
    public Optional<String> getCurrentAuditor() {
        // 세션에서 로그인된 사용자 정보 가져오기
        User sessionUser = (User) httpSession.getAttribute(SessionConst.LOGIN_USER);

        // 로그인된 사용자가 있다면 그 사용자의 username을 반환
        if (sessionUser != null) {
            return Optional.of(sessionUser.getUsername()); // 로그인된 사용자의 username을 반환
        }

        // 로그인되지 않은 경우 "anonymous" 반환
        return Optional.of("anonymous");
    }
}
