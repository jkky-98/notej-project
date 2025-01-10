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
    @JoinColumn(name = "user_id")
    private User user;

    // 발신자 (알림을 발생시킨 사용자)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;  // 알림을 발생시킨 사용자 (발신자)

    public void updateUser(User user) {
        this.user = user;
    }
}
