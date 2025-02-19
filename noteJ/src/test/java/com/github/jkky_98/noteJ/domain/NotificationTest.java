package com.github.jkky_98.noteJ.domain;

import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.domain.user.UserDesc;
import com.github.jkky_98.noteJ.domain.user.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class NotificationTest {
    @Test
    @DisplayName("[Notification] Builder 패턴 테스트")
    void notificationBuilderTest() {
        // given: 테스트용 User 객체 생성
        User sender = createTestUser("senderUser", "sender@example.com");
        User receiver = createTestUser("receiverUser", "receiver@example.com");

        // when: Notification 객체 빌더를 통해 생성
        Notification notification = Notification.builder()
                .status(false)
                .message("테스트 알림 메시지")
                .type(NotificationType.LIKE)
                .sender(sender)
                .receiver(receiver)
                .build();

        // then: 각 필드가 올바르게 설정되었는지 검증
        assertThat(notification.isStatus()).isFalse();
        assertThat(notification.getMessage()).isEqualTo("테스트 알림 메시지");
        assertThat(notification.getType()).isEqualTo(NotificationType.LIKE);
        assertThat(notification.getSender()).isEqualTo(sender);
        assertThat(notification.getReceiver()).isEqualTo(receiver);
    }

    @Test
    @DisplayName("[Notification] 기본 상태 테스트")
    void notificationDefaultStateTest() {
        // given: 기본 빌더로 Notification 객체 생성 (아무 값도 설정하지 않음)
        Notification notification = Notification.builder().build();

        // then: 기본 상태 값 검증
        // id는 아직 DB에 저장되지 않았으므로 null
        // boolean 타입인 status는 기본값 false,
        // message, type, receiver, sender 등은 기본적으로 null이어야 함.
        assertThat(notification.getId()).isNull();
        assertThat(notification.isStatus()).isFalse();
        assertThat(notification.getMessage()).isNull();
        assertThat(notification.getType()).isNull();
        assertThat(notification.getReceiver()).isNull();
        assertThat(notification.getSender()).isNull();
    }

    @Test
    @DisplayName("[Notification] updateReceiver 메서드 테스트")
    void updateReceiverTest() {
        // given: 테스트용 User 객체 생성
        User originalReceiver = createTestUser("receiverUser", "receiver@example.com");
        User newReceiver = createTestUser("newReceiver", "newreceiver@example.com");

        Notification notification = Notification.builder()
                .status(false)
                .message("알림 메시지")
                .type(NotificationType.LIKE)
                .receiver(originalReceiver)
                .build();

        // when: updateReceiver 호출하여 수신자 업데이트
        notification.updateReceiver(newReceiver);

        // then: 업데이트된 수신자가 newReceiver인지 검증
        assertThat(notification.getReceiver()).isEqualTo(newReceiver);
    }

    @Test
    @DisplayName("[Notification] updateSender 메서드 테스트")
    void updateSenderTest() {
        // given: 테스트용 User 객체 생성
        User originalSender = createTestUser("senderUser", "sender@example.com");
        User newSender = createTestUser("newSender", "newsender@example.com");

        Notification notification = Notification.builder()
                .status(false)
                .message("알림 메시지")
                .type(NotificationType.LIKE)
                .sender(originalSender)
                .build();

        // when: updateSender 호출하여 발신자 업데이트
        notification.updateSender(newSender);

        // then: 업데이트된 발신자가 newSender인지 검증
        assertThat(notification.getSender()).isEqualTo(newSender);
    }

    @Test
    @DisplayName("[Notification] readNotification 메서드 테스트")
    void readNotificationTest() {
        // given: status가 false인 Notification 객체 생성
        Notification notification = Notification.builder()
                .status(false)
                .message("읽기 전 테스트 알림")
                .type(NotificationType.LIKE)
                .build();

        // when: readNotification 호출하면 status가 true로 변경되어야 함
        notification.readNotification();
        assertThat(notification.isStatus()).isTrue();

        // then: 이미 읽은 상태에서 다시 readNotification 호출하면 예외가 발생해야 함
        assertThatThrownBy(notification::readNotification)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 읽은 Notification");
    }

    // 테스트용 User 객체 생성 헬퍼 메서드
    private User createTestUser(String username, String email) {
        UserDesc userDesc = UserDesc.builder()
                .description("테스트 유저 설명")
                .blogTitle("테스트 블로그")
                .build();

        return User.builder()
                .username(username)
                .email(email)
                .password("password123")
                .userRole(UserRole.USER)
                .userDesc(userDesc)
                .build();
    }
}
