package com.github.jkky_98.noteJ.web.controller;

import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.service.NotificationService;
import com.github.jkky_98.noteJ.web.controller.dto.NotificationDto;
import com.github.jkky_98.noteJ.web.session.SessionConst;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/notifications")
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping()
    public String notifications(Model model,
                                @SessionAttribute(SessionConst.LOGIN_USER) User sessionUser,
                                @RequestParam(value = "status", required = false) Optional<Boolean> status
    ) {
        List<NotificationDto> notifications = notificationService.getNotification(sessionUser, status);
        model.addAttribute("notifications", notifications);
        return "notifications";
    }

    // 2. 모두 읽음 처리
    @PostMapping("/read-all")
    public ResponseEntity<?> markAllAsRead(@SessionAttribute(SessionConst.LOGIN_USER) User sessionUser) {
        notificationService.readNotificationAll(sessionUser);
        Map<String, String> response = new HashMap<>();
        response.put("message", "All notifications marked as read.");
        return ResponseEntity.ok(response);
    }

    // 3. 모두 삭제
    @PostMapping("/delete-all")
    public ResponseEntity<?> deleteAllNotifications(@SessionAttribute(SessionConst.LOGIN_USER) User sessionUser) {
        notificationService.deleteNotificationAll(sessionUser);
        Map<String, String> response = new HashMap<>();
        response.put("message", "All notifications deleted.");
        return ResponseEntity.ok(response);
    }

    // 4. 개별 알림 읽음 처리
    @PostMapping("/read/{id}")
    public ResponseEntity<?> markAsRead(@PathVariable Long id,
                                        @SessionAttribute(SessionConst.LOGIN_USER) User sessionUser) {
        notificationService.readNotificationOne(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Notification marked as read.");
        return ResponseEntity.ok(response);
    }
}
