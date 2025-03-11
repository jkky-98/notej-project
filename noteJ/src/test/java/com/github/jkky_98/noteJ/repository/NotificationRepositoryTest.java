package com.github.jkky_98.noteJ.repository;

import com.github.jkky_98.noteJ.domain.Notification;
import com.github.jkky_98.noteJ.domain.NotificationType;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.domain.user.UserRole;
import com.github.jkky_98.noteJ.web.config.CacheConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)  // 기본 H2 사용 막기
@ActiveProfiles("test")
@Import({CacheConfig.class})
@DisplayName("[NotificationRepository] Integration Tests")
class NotificationRepositoryTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private NotificationRepository notificationRepository;

    private User receiver;
    private User sender;
    private Notification notification1;
    private Notification notification2;
    private Notification notificationRead;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        receiver = User.builder()
                .username("receiver")
                .email("receiver@test.com")
                .password("123456")
                .userRole(UserRole.USER)
                .build();
        sender = User.builder()
                .username("sender")
                .email("sender@test.com")
                .userRole(UserRole.USER)
                .password("123456")
                .build();
        em.persist(receiver);
        em.persist(sender);

        // 읽지 않은 알림 2건 생성
        notification1 = Notification.builder()
                .type(NotificationType.FOLLOW)
                .message("sender님으로부터 팔로우 되었습니다.")
                .status(false)
                .sender(sender)
                .receiver(receiver)
                .build();
        notification2 = Notification.builder()
                .type(NotificationType.LIKE)
                .message("sender님이 당신의 게시글에 좋아요를 눌렀습니다.")
                .status(false)
                .sender(sender)
                .receiver(receiver)
                .build();

        // 읽은 알림 1건 생성
        notificationRead = Notification.builder()
                .type(NotificationType.COMMENT)
                .message("sender님이 당신의 게시글에 댓글을 남겼습니다.")
                .status(true)
                .sender(sender)
                .receiver(receiver)
                .build();

        em.persist(notification1);
        em.persist(notification2);
        em.persist(notificationRead);

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("findAllNotificationsByReceiver() - 전체 알림 조회")
    void testFindAllNotificationsByReceiver() {
        List<Notification> notifications = notificationRepository.findAllNotificationsByReceiver(receiver);
        assertThat(notifications).hasSize(3);
    }

    @Test
    @DisplayName("countUnreadNotificationsByReceiver() - 읽지 않은 알림 개수 조회")
    void testCountUnreadNotificationsByReceiver() {
        long count = notificationRepository.countUnreadNotificationsByReceiver(receiver);
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("findAllUnreadNotificationsByReceiver() - 읽지 않은 알림 목록 조회")
    void testFindAllUnreadNotificationsByReceiver() {
        List<Notification> unreadNotifications = notificationRepository.findAllUnreadNotificationsByReceiver(receiver);
        assertThat(unreadNotifications).hasSize(2);
        unreadNotifications.forEach(n -> assertThat(n.isStatus()).isFalse());
    }

    // --- 경계/실패 케이스 테스트 ---

    @Test
    @DisplayName("findAllNotificationsByReceiver: 알림이 없는 경우 빈 리스트 반환")
    void testFindAllNotificationsByReceiver_noNotifications() {
        // 새로운 사용자 생성 (알림이 없음)
        User newUser = User.builder()
                .username("noNotiUser")
                .email("noNotiUser@test.com")
                .userRole(UserRole.USER)
                .password("123456")
                .build();
        em.persist(newUser);
        em.flush();
        em.clear();

        List<Notification> notifications = notificationRepository.findAllNotificationsByReceiver(newUser);
        assertThat(notifications).isEmpty();
    }

    @Test
    @DisplayName("countUnreadNotificationsByReceiver() - 알림이 없는 경우 0 반환")
    void testCountUnreadNotificationsByReceiver_noNotifications() {
        // 새로운 사용자 생성 (알림이 없음)
        User newUser = User.builder()
                .username("noNotiUser")
                .email("noNotiUser@test.com")
                .userRole(UserRole.USER)
                .password("123456")
                .build();
        em.persist(newUser);
        em.flush();
        em.clear();

        long count = notificationRepository.countUnreadNotificationsByReceiver(newUser);
        assertThat(count).isEqualTo(0);
    }

    @Test
    @DisplayName("findAllUnreadNotificationsByReceiver() - 읽은 알림만 있는 경우 빈 리스트 반환")
    void testFindAllUnreadNotificationsByReceiver_onlyReadNotifications() {
        // 새로운 사용자 생성 및 알림 등록: 모두 읽은 알림만 등록
        User readUser = User.builder()
                .username("readUser")
                .email("readUser@test.com")
                .userRole(UserRole.USER)
                .password("123456")
                .build();
        em.persist(readUser);

        Notification readNoti1 = Notification.builder()
                .type(NotificationType.FOLLOW)
                .message("sender님으로부터 팔로우 되었습니다.")
                .status(true)  // 읽은 상태
                .sender(sender)
                .receiver(readUser)
                .build();
        Notification readNoti2 = Notification.builder()
                .type(NotificationType.LIKE)
                .message("sender님이 당신의 게시글에 좋아요를 눌렀습니다.")
                .status(true)  // 읽은 상태
                .sender(sender)
                .receiver(readUser)
                .build();
        em.persist(readNoti1);
        em.persist(readNoti2);
        em.flush();
        em.clear();

        List<Notification> unreadNotifications = notificationRepository.findAllUnreadNotificationsByReceiver(readUser);
        assertThat(unreadNotifications).isEmpty();
    }
}

