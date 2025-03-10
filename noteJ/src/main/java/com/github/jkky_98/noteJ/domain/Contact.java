package com.github.jkky_98.noteJ.domain;

import com.github.jkky_98.noteJ.domain.base.BaseEntity;
import com.github.jkky_98.noteJ.domain.user.User;
import jakarta.persistence.*;
import lombok.*;
// toDo : test
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 기본 생성자
@AllArgsConstructor(access = AccessLevel.PRIVATE) // 빌더와 함께 사용할 모든 필드 생성자
public class Contact extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contact_id", nullable = false)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String content;

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;
}
