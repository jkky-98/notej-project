package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.Notification;
import com.github.jkky_98.noteJ.domain.mapper.NotificationMapper;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.repository.NotificationRepository;
import com.github.jkky_98.noteJ.web.controller.dto.NotificationForm;
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

    @Mock
    private NotificationMapper notificationMapper;

    @InjectMocks
    private NotificationService notificationService;

    @Spy
    private User userGet;  // 알림 수신자

    @Spy
    private User userSend; // 알림 발송자

    private Notification notification;

    @BeforeEach
    void setUp() {
        userGet = spy(User.builder().id(100L).username("receiver").build());
        userSend = spy(User.builder().id(200L).username("sender").build());

        notification = Notification.ofFollow(userSend, userGet);
    }


    @Test
    @DisplayName("sendFollowNotification() - 팔로우 알림 전송")
    void testSendFollowNotification() {
        when(notificationMapper.toNotificationForFollow(userGet, userSend)).thenReturn(notification);
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        notificationService.sendFollowNotification(userGet, userSend);

        verify(notificationRepository, times(1)).save(notification);
        verify(userGet, times(1)).addNotificationToRecipient(notification);
        verify(userSend, times(1)).addNotificationToSender(notification);
    }

    @Test
    @DisplayName("sendLikePostNotification() - 좋아요 알림 전송")
    void testSendLikePostNotification() {
        String postTitle = "Test Post";
        when(notificationMapper.toNotificationForLike(userSend, userGet, postTitle)).thenReturn(notification);
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        notificationService.sendLikePostNotification(userGet, userSend, postTitle);

        verify(notificationRepository, times(1)).save(notification);
        verify(userGet, times(1)).addNotificationToRecipient(notification);
        verify(userSend, times(1)).addNotificationToSender(notification);
    }

    @Test
    @DisplayName("sendCommentPostNotification() - 댓글 알림 전송")
    void testSendCommentPostNotification() {
        String postTitle = "Test Post";
        when(notificationMapper.toNotificationForComment(userSend, userGet, postTitle)).thenReturn(notification);
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        notificationService.sendCommentPostNotification(userGet, userSend, postTitle);

        verify(notificationRepository, times(1)).save(notification);
        verify(userGet, times(1)).addNotificationToRecipient(notification);
        verify(userSend, times(1)).addNotificationToSender(notification);
    }

    @Test
    @DisplayName("sendCommentParentsNotification() - 대댓글 알림 전송")
    void testSendCommentParentsNotification() {
        String postTitle = "Test Post";
        when(notificationMapper.toNotificationForCommentParents(userSend, userGet, postTitle)).thenReturn(notification);
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        notificationService.sendCommentParentsNotification(userGet, userSend, postTitle);

        verify(notificationRepository, times(1)).save(notification);
        verify(userGet, times(1)).addNotificationToRecipient(notification);
        verify(userSend, times(1)).addNotificationToSender(notification);
    }

    @Test
    @DisplayName("getNotification() - 전체 알림 조회")
    void testGetNotification_all() {
        Long sessionUserId = 100L;
        List<Notification> notifications = List.of(notification, Notification.ofFollow(userSend, userGet));
        when(userService.findUserById(sessionUserId)).thenReturn(userGet);
        when(notificationRepository.findAllNotificationsByReceiver(userGet)).thenReturn(notifications);
        when(notificationMapper.toNotificationFormList(notifications)).thenReturn(new ArrayList<>());

        List<NotificationForm> result = notificationService.getNotification(sessionUserId, Optional.empty());

        assertNotNull(result);
        assertEquals(0, result.size()); // Mock이므로 빈 리스트
        verify(userService).findUserById(sessionUserId);
        verify(notificationRepository).findAllNotificationsByReceiver(userGet);
    }

    @Test
    @DisplayName("getNotification() - 읽지 않은 알림만 조회")
    void testGetNotification_unreadOnly() {
        Long sessionUserId = 100L;
        List<Notification> unreadNotifications = List.of(notification);
        when(userService.findUserById(sessionUserId)).thenReturn(userGet);
        when(notificationRepository.findAllUnreadNotificationsByReceiver(userGet)).thenReturn(unreadNotifications);
        when(notificationMapper.toNotificationFormList(unreadNotifications)).thenReturn(new ArrayList<>());

        List<NotificationForm> result = notificationService.getNotification(sessionUserId, Optional.of(false));

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(notificationRepository).findAllUnreadNotificationsByReceiver(userGet);
    }

    @Test
    @DisplayName("getNotificationCountNotRead() - 읽지 않은 알림 개수 반환")
    void testGetNotificationCountNotRead() {
        Long sessionUserId = 100L;
        when(userService.findUserById(sessionUserId)).thenReturn(userGet);
        when(notificationRepository.countUnreadNotificationsByReceiver(userGet)).thenReturn(5L);

        Long count = notificationService.getNotificationCountNotRead(sessionUserId);

        assertEquals(5L, count);
        verify(notificationRepository).countUnreadNotificationsByReceiver(userGet);
    }

    @Test
    @DisplayName("readNotificationOne() - 단건 알림 읽음 처리")
    void testReadNotificationOne() {
        Long notificationId = 999L;
        Notification notif = spy(notification);
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notif));

        notificationService.readNotificationOne(notificationId);

        verify(notif).readNotification();
    }

    @Test
    @DisplayName("readNotificationAll() - 모든 알림 읽음 처리")
    void testReadNotificationAll() {
        List<Notification> notifications = List.of(spy(notification), spy(notification));
        when(notificationRepository.findAllNotificationsByReceiver(userGet)).thenReturn(notifications);

        notificationService.readNotificationAll(userGet);

        notifications.forEach(notif -> verify(notif).readNotification());
    }

    @Test
    @DisplayName("deleteNotificationAll() - 모든 알림 삭제")
    void testDeleteNotificationAll() {
        List<Notification> notifications = List.of(notification, Notification.ofFollow(userSend, userGet));
        when(notificationRepository.findAllNotificationsByReceiver(userGet)).thenReturn(notifications);

        notificationService.deleteNotificationAll(userGet);

        verify(notificationRepository).deleteAll(notifications);
    }
}