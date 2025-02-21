package com.github.jkky_98.noteJ.web.session;

import com.github.jkky_98.noteJ.domain.user.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.Optional;

public class SessionUtils {

    public static Optional<User> getSessionUser(HttpServletRequest request) {
        User sessionUser = (User) request.getSession().getAttribute(SessionConst.LOGIN_USER);

        return Optional.ofNullable(sessionUser);
    }

    public static Optional<User> getSessionUser(HttpSession session) {
        User sessionUser = (User) session.getAttribute(SessionConst.LOGIN_USER);

        return Optional.ofNullable(sessionUser);
    }
}
