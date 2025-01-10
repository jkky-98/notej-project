package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.Notification;
import com.github.jkky_98.noteJ.domain.NotificationType;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.repository.NotificationRepository;
import com.github.jkky_98.noteJ.web.controller.dto.NotificationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void sendFollowNotification(User userGetNotification, User userSendNotification) {
        // 알림 엔티티 생성 for 팔로우
        Notification notification = Notification.builder()
                .type(NotificationType.FOLLOW)
                .message(userSendNotification.getUsername() + "님으로부터 팔로우 되었습니다.")
                .status(false)
                .sender(userSendNotification)
                .user(userGetNotification)
                .build();

        // 알림 저장
        notificationRepository.save(notification);

        userGetNotification.addReceivedNotification(notification); // 수신자에 알림 추가
        userSendNotification.addSentNotification(notification); // 발송자에 알림 추가
    }

    @Transactional
    public List<NotificationDto> getNotification(User sessionUser) {

        List<NotificationDto> returnList = new ArrayList<>();

        for (Notification notification : notificationRepository.findAllNotificationsByUser(sessionUser)) {
            NotificationDto notificationDto = new NotificationDto();
            notificationDto.setMessage(notification.getMessage());
            notificationDto.setType(notification.getType().name());
            notificationDto.setStatus(notification.isStatus());
            notificationDto.setCreateTime(notification.getCreateDt());

            returnList.add(notificationDto);
        }

        return returnList;
    }

    @Transactional
    public Long getNotificationCountNotRead(User sessionUser) {
        return notificationRepository.countUnreadNotificationsByUser(sessionUser);
    }
}
