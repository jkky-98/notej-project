package com.github.jkky_98.noteJ.filter;


import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.domain.user.UserRole;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

import java.io.IOException;
@Component
public class ActuatorAccessFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String path = httpRequest.getRequestURI();

        // /actuator/prometheus는 인증 없이 통과하도록 예외 처리
        if (path.startsWith("/actuator") && !path.equals("/actuator/prometheus")) {
            HttpSession session = httpRequest.getSession(false);
            User currentUser = (session != null) ? (User) session.getAttribute("loginUser") : null;
            // 사용자 정보가 없거나 ADMIN 권한이 아닌 경우 403 응답
            if (currentUser == null || currentUser.getUserRole() != UserRole.ADMIN) {
                HttpServletResponse httpResponse = (HttpServletResponse) response;
                httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                request.getRequestDispatcher("/error/403").forward(request, response);
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
