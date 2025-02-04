package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.Notification;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.exception.UnauthenticatedUserException;
import com.github.jkky_98.noteJ.repository.NotificationRepository;
import com.github.jkky_98.noteJ.web.controller.dto.NotificationDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserService userService;

    /**
     * Follow 신청 알림 보내기
     * @param userGetNotification
     * @param userSendNotification
     */
    @Transactional
    public void sendFollowNotification(User userGetNotification, User userSendNotification) {
        // 알림 엔티티 생성 for 팔로우
        Notification notification = Notification.ofFollow(userSendNotification, userGetNotification);
        // 알림 저장
        notificationRepository.save(notification);

        userGetNotification.addNotificationToRecipient(notification); // 수신자에 알림 추가
        userSendNotification.addNotificationToSender(notification); // 발송자에 알림 추가
    }

    @Transactional
    public void sendLikePostNotification(User userGetNotification, User userSendNotification, String postTitle) {
        Notification notification = Notification.ofLike(userSendNotification, userGetNotification, postTitle);
        notificationRepository.save(notification);

        userGetNotification.addNotificationToRecipient(notification);
        userSendNotification.addNotificationToSender(notification);
    }

    @Transactional
    public void sendCommentPostNotification(User userGetNotification, User userSendNotification, String postTitle) {
        Notification notification = Notification.ofComment(userSendNotification, userGetNotification, postTitle);
        notificationRepository.save(notification);

        userGetNotification.addNotificationToRecipient(notification);
        userSendNotification.addNotificationToSender(notification);
    }

    @Transactional
    public void sendCommentParentsNotification(User userGetNotification, User userSendNotification, String postTitle) {
        Notification notification = Notification.ofCommentParents(userSendNotification, userGetNotification, postTitle);
        notificationRepository.save(notification);

        userGetNotification.addNotificationToRecipient(notification);
        userSendNotification.addNotificationToSender(notification);
    }

    /**
     * 유저 알림 모두 조회
     * @param sessionUser
     * @return
     */
    @Transactional(readOnly = true)
    public List<NotificationDto> getNotification(User sessionUser, Optional<Boolean> status) {
        List<Notification> notifications = filterNotificationsByStatus(sessionUser, status);

        return convertNotificationsToDto(notifications);
    }

    /**
     * 안 읽은 알림 수 가져오기
     * @param sessionUserId
     * @return
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "navigationAlarm", key = "#sessionUserId")
    public Long getNotificationCountNotRead(Long sessionUserId) {

        User user = userService.findUserById(sessionUserId);

        return notificationRepository.countUnreadNotificationsByReceiver(user);
    }

    /**
     * 알림 단건 읽기
     * @param notificationId
     */
    @Transactional
    public void readNotificationOne(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElseThrow(() -> new EntityNotFoundException("notification entity not found"));
        notification.readNotification();
    }

    /**
     * 알림 모두 읽기
     * @param sessionUser
     */
    @Transactional
    public void readNotificationAll(User sessionUser) {
        if (sessionUser == null) {
            throw new UnauthenticatedUserException("not authenticated");
        }

        notificationRepository.findAllNotificationsByReceiver(sessionUser).stream()
                .filter(notification -> !notification.isStatus())
                .forEach(Notification::readNotification);
    }

    /**
     * 알림 모두 삭제
     * @param sessionUser
     */
    @Transactional
    public void deleteNotificationAll(User sessionUser) {
        List<Notification> notificationList = notificationRepository.findAllNotificationsByReceiver(sessionUser);
        notificationRepository.deleteAll(notificationList);
    }


    private List<NotificationDto> convertNotificationsToDto(List<Notification> notifications) {
        return notifications.stream()
                .map(NotificationDto::of)
                .toList();
    }

    private List<Notification> filterNotificationsByStatus(User sessionUser, Optional<Boolean> status) {
        List<Notification> notifications = status
                .filter(s -> !s) // status가 false일 경우 필터 통과
                .map(s -> notificationRepository.findAllUnreadNotificationsByReceiver(sessionUser))
                .orElseGet(() -> notificationRepository.findAllNotificationsByReceiver(sessionUser)); // Optional이 empty거나 다른 값인 경우
        return notifications;
    }
}
