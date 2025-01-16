package com.github.jkky_98.noteJ.domain;

import com.github.jkky_98.noteJ.domain.base.BaseEntity;
import com.github.jkky_98.noteJ.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Notification extends BaseEntity {
    @Id @GeneratedValue
    @Column(name = "notification_id")
    private Long id;

    private boolean status;

    private String message;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    // 연관관계
    // 수신자 (알림을 받는 사용자)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    private User receiver;

    // 발신자 (알림을 발생시킨 사용자)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;  // 알림을 발생시킨 사용자 (발신자)

    public void updateReceiver(User user) {
        this.receiver = user;
    }

    public void updateSender(User user) {
        this.sender = user;
    }

    public void readNotification() {
        if (status == false) {
            this.status = true;
        } else {
            throw new IllegalStateException("이미 읽은 Notification을 읽음 처리하려고 하고 있습니다.");
        }
    }

    public static Notification ofFollow(User userSendNotification, User userGetNotification) {
        return Notification.builder()
                .type(NotificationType.FOLLOW)
                .message(userSendNotification.getUsername() + "님으로부터 팔로우 되었습니다.")
                .status(false)
                .sender(userSendNotification)
                .receiver(userGetNotification)
                .build();
    }

    public static Notification ofLike(User userSendNotification, User userGetNotification, String postTitle) {
        return Notification.builder()
                .type(NotificationType.LIKE)
                .message(userSendNotification.getUsername() + "님이 당신의 게시글 : " + postTitle + "에 좋아요를 눌렀습니다.")
                .status(false)
                .sender(userSendNotification)
                .receiver(userGetNotification)
                .build();
    }
}
