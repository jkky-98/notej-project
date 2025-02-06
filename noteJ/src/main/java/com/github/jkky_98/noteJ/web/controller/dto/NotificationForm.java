package com.github.jkky_98.noteJ.web.controller.dto;

import com.github.jkky_98.noteJ.domain.Notification;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NotificationForm {
    private Long id;
    private boolean status;
    private String message;
    private String type;
    private String usernameSender;
    private LocalDateTime createTime;

    public static NotificationForm of(Notification notification) {
        return NotificationForm.builder()
                .id(notification.getId())
                .message(notification.getMessage())
                .type(notification.getType().name())
                .status(notification.isStatus())
                .createTime(notification.getCreateDt())
                .build();
    }
}
