package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.Notification;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.repository.NotificationRepository;
import com.github.jkky_98.noteJ.web.controller.dto.NotificationForm;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("[NotificationService] Unit Tests")
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private NotificationService notificationService;

    private User userGet;     // 알림 수신자
    private User userSend;    // 알림 발송자
    private Notification notification;

    @BeforeEach
    void setUp() {
        // spy 처리하여 addNotificationToRecipient, addNotificationToSender 호출 검증 가능
        userGet = spy(User.builder()
                .id(100L)
                .username("receiver")
                .build());
        userSend = spy(User.builder()
                .id(200L)
                .username("sender")
                .build());

        // 테스트용 알림 객체 (각 static 팩토리 메서드 Notification.ofFollow, ofLike, ofComment 등 호출 결과를 가정)
        notification = Notification.ofFollow(userSend, userGet);
    }

    @Test
    @DisplayName("[NotificationService] sendFollowNotification: 팔로우 알림 전송")
    void testSendFollowNotification() {
        // given
        // notificationRepository.save() 호출 시 생성된 notification를 반환하도록 stub
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        // when
        notificationService.sendFollowNotification(userGet, userSend);

        // then
        // notificationRepository.save() 호출 여부 검증
        verify(notificationRepository, times(1)).save(any(Notification.class));
        // 수신자/발송자에 알림 추가 호출 여부 검증 (spy 객체로 검증)
        verify(userGet, times(1)).addNotificationToRecipient(any(Notification.class));
        verify(userSend, times(1)).addNotificationToSender(any(Notification.class));
    }

    @Test
    @DisplayName("[NotificationService] sendLikePostNotification: 좋아요 알림 전송")
    void testSendLikePostNotification() {
        // given
        String postTitle = "Test Post Title";
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        // when
        notificationService.sendLikePostNotification(userGet, userSend, postTitle);

        // then
        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(userGet, times(1)).addNotificationToRecipient(any(Notification.class));
        verify(userSend, times(1)).addNotificationToSender(any(Notification.class));
    }

    @Test
    @DisplayName("[NotificationService] sendCommentPostNotification: 댓글 알림 전송")
    void testSendCommentPostNotification() {
        // given
        String postTitle = "Test Post Title";
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        // when
        notificationService.sendCommentPostNotification(userGet, userSend, postTitle);

        // then
        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(userGet, times(1)).addNotificationToRecipient(any(Notification.class));
        verify(userSend, times(1)).addNotificationToSender(any(Notification.class));
    }

    @Test
    @DisplayName("[NotificationService] sendCommentParentsNotification: 대댓글 알림 전송")
    void testSendCommentParentsNotification() {
        // given
        String postTitle = "Test Post Title";
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        // when
        notificationService.sendCommentParentsNotification(userGet, userSend, postTitle);

        // then
        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(userGet, times(1)).addNotificationToRecipient(any(Notification.class));
        verify(userSend, times(1)).addNotificationToSender(any(Notification.class));
    }

    @Test
    @DisplayName("[NotificationService] getNotification: 알림 조회 - 전체 알림")
    void testGetNotification_all() {
        // given
        Long sessionUserId = 100L;
        List<Notification> allNotifications = new ArrayList<>();
        // 예시로 2개의 알림 객체 추가
        allNotifications.add(notification);
        allNotifications.add(Notification.ofFollow(userSend, userGet));

        when(userService.findUserById(sessionUserId)).thenReturn(userGet);
        // status가 Optional.empty()인 경우 전체 알림 반환
        when(notificationRepository.findAllNotificationsByReceiver(userGet)).thenReturn(allNotifications);

        // when
        List<NotificationForm> result = notificationService.getNotification(sessionUserId, Optional.empty());

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userService).findUserById(sessionUserId);
        verify(notificationRepository).findAllNotificationsByReceiver(userGet);
    }

    @Test
    @DisplayName("[NotificationService] getNotification: 알림 조회 - 읽지 않은 알림만")
    void testGetNotification_unreadOnly() {
        // given
        Long sessionUserId = 100L;
        List<Notification> unreadNotifications = new ArrayList<>();
        unreadNotifications.add(notification);

        when(userService.findUserById(sessionUserId)).thenReturn(userGet);
        when(notificationRepository.findAllUnreadNotificationsByReceiver(userGet)).thenReturn(unreadNotifications);

        // when
        List<NotificationForm> result = notificationService.getNotification(sessionUserId, Optional.of(false));

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userService).findUserById(sessionUserId);
        verify(notificationRepository).findAllUnreadNotificationsByReceiver(userGet);
    }

    @Test
    @DisplayName("[NotificationService] getNotificationCountNotRead: 읽지 않은 알림 개수 반환")
    void testGetNotificationCountNotRead() {
        // given
        Long sessionUserId = 100L;
        when(userService.findUserById(sessionUserId)).thenReturn(userGet);
        when(notificationRepository.countUnreadNotificationsByReceiver(userGet)).thenReturn(5L);

        // when
        Long count = notificationService.getNotificationCountNotRead(sessionUserId);

        // then
        assertEquals(5L, count);
        verify(userService).findUserById(sessionUserId);
        verify(notificationRepository).countUnreadNotificationsByReceiver(userGet);
    }

    @Test
    @DisplayName("[NotificationService] readNotificationOne: 단건 알림 읽음 처리")
    void testReadNotificationOne() {
        // given
        Long notificationId = 999L;
        Notification notif = spy(notification);
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notif));

        // when
        assertDoesNotThrow(() -> notificationService.readNotificationOne(notificationId));

        // then
        verify(notificationRepository).findById(notificationId);
        verify(notif).readNotification();
    }

    @Test
    @DisplayName("[NotificationService] readNotificationAll: 모든 알림 읽음 처리")
    void testReadNotificationAll() {
        // given
        // userGet은 이미 setUp()에서 생성된 spy 객체라고 가정하고, 다시 spy하지 않고 그대로 사용
        User receiver = userGet;

        // 두 개의 알림 객체를 생성하고, spy로 감싸서 readNotification 호출을 검증
        Notification notif1 = spy(Notification.ofFollow(userSend, receiver));
        Notification notif2 = spy(Notification.ofFollow(userSend, receiver));

        // 두 알림 모두 미읽음 상태로 설정
        when(notif1.isStatus()).thenReturn(false);
        when(notif2.isStatus()).thenReturn(false);

        List<Notification> allNotifications = List.of(notif1, notif2);
        when(notificationRepository.findAllNotificationsByReceiver(receiver)).thenReturn(allNotifications);

        // when
        notificationService.readNotificationAll(receiver);

        // then
        verify(notificationRepository).findAllNotificationsByReceiver(receiver);
        verify(notif1).readNotification();
        verify(notif2).readNotification();
    }


    @Test
    @DisplayName("deleteNotificationAll: 모든 알림 삭제")
    void testDeleteNotificationAll() {
        // given
        List<Notification> notifications = new ArrayList<>();
        notifications.add(notification);
        notifications.add(Notification.ofFollow(userSend, userGet));
        when(notificationRepository.findAllNotificationsByReceiver(userGet)).thenReturn(notifications);

        // when
        notificationService.deleteNotificationAll(userGet);

        // then
        verify(notificationRepository).findAllNotificationsByReceiver(userGet);
        verify(notificationRepository).deleteAll(notifications);
    }
}