package com.github.jkky_98.noteJ.web.controller.global;

import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.service.GlobalService;
import com.github.jkky_98.noteJ.service.NotificationService;
import com.github.jkky_98.noteJ.web.controller.form.UserViewForm;
import com.github.jkky_98.noteJ.web.session.SessionConst;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.SessionAttribute;


@ControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalControllerAdvice {

    private final NotificationService notificationService;
    private final GlobalService globalService;

    @ModelAttribute("currentUrl")
    public String currentUrl(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        // 특정 URL에서는 실행하지 않음
        if (    requestUri.startsWith("/api/") ||
                requestUri.startsWith("/editor/") ||
                requestUri.startsWith("/image-print/")
        )
        {
            return null;
        }
        String queryString = request.getQueryString();
        return request.getRequestURI() + (queryString != null ? "?" + queryString : "");
    }

    @ModelAttribute("currentUri")
    public String currentUri(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        // 특정 URL에서는 실행하지 않음
        if (    requestUri.startsWith("/api/") ||
                requestUri.startsWith("/editor/") ||
                requestUri.startsWith("/image-print/")
        )
        {
            return null;
        }
        return request.getRequestURI();
    }


    @ModelAttribute
    public void addSessionUserToModel(
            @SessionAttribute(value = SessionConst.LOGIN_USER, required = false) User loginUser,
            Model model, HttpServletRequest request) {

        String requestUri = request.getRequestURI();

        // 특정 URL에서는 실행하지 않음
        if (    requestUri.startsWith("/api/") ||
                requestUri.startsWith("/editor/") ||
                requestUri.startsWith("/image-print/")
        )
        {
            return;
        }

        // 세션에서 사용자 ID 가져오기
        if (loginUser == null) {
            return; // 세션에 사용자 정보가 없으면 바로 반환
        }

        UserViewForm navigationWithSessionUser = globalService.getNavigationWithSessionUser(loginUser.getId());
        model.addAttribute("sessionUser", navigationWithSessionUser);

        // 알림 수 가져오기
        Long notificationCountNotRead = notificationService.getNotificationCountNotRead(loginUser.getId());
        model.addAttribute("notificationCountNotRead", notificationCountNotRead);
    }

}
