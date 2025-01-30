package com.github.jkky_98.noteJ.filter;

import com.github.jkky_98.noteJ.web.session.SessionConst;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;
import java.net.URLEncoder;

@Slf4j
public class AuthorizationUsernamePasswordFilter implements Filter {

    private static final String[] WHITE_LIST = {
            "/",
            "/login",
            "/logout",
            "/signup",
            "/css/**",
            "/js/**",
            "/img/**",
            "/storage/**",
            "/@*/posts/**",
            "/@*/post/*",
            "/image-print/**",
            "/image-upload/**",
            "/editor-image-upload/**",
            "/@*/followings",
            "/@*/followers",
            "/api/@*/posts/**",
            "/api/post/likes/**"
    };
    private static final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestURI = httpRequest.getRequestURI();

        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {
            if (isLoginCheckPath(requestURI)) {
                HttpSession session = httpRequest.getSession(false);
                if (session == null || session.getAttribute(SessionConst.LOGIN_USER) == null) {
                    httpResponse.sendRedirect("/login?redirectURL=" + URLEncoder.encode(requestURI, "UTF-8"));
                    return;
                }
            }
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("Filter Error: not Authorization : {} ", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 화이트 리스트의 경우 인증 체크X
     */
    private boolean isLoginCheckPath(String requestURI) {
        if (requestURI.startsWith("/login")) {
            return false; // /login 및 /login?redirectURL=... 제외
        }

        for (String pattern : WHITE_LIST) {
            if (pathMatcher.match(pattern, requestURI)) {
                return false;
            }
        }
        return true;
    }

}
