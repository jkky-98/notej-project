package com.github.jkky_98.noteJ.web.controller;

import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.service.NotificationService;
import com.github.jkky_98.noteJ.web.controller.dto.NotificationDto;
import com.github.jkky_98.noteJ.web.session.SessionConst;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/notifications")
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping()
    public String notifications(Model model,
                                @SessionAttribute(SessionConst.LOGIN_USER) User sessionUser) {
        List<NotificationDto> notifications = notificationService.getNotification(sessionUser);
        model.addAttribute("notifications", notifications);
        log.info("알림 뷰로 모델 전송 시도");
        log.info("notifications: {}", notifications);
        return "notifications";
    }
}
