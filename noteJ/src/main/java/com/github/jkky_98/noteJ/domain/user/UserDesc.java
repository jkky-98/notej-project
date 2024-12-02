package com.github.jkky_98.noteJ.domain.user;

import com.github.jkky_98.noteJ.domain.base.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 기본 생성자
@AllArgsConstructor(access = AccessLevel.PRIVATE) // 빌더와 함께 사용할 모든 필드 생성자
public class UserDesc extends BaseTimeEntity {
    @Id
    @GeneratedValue
    @Column(name = "user_desc_id")
    private Long id;


    private String description;

    // 연관관계
    @OneToOne(fetch = FetchType.LAZY,
            mappedBy = "userDesc")
    private User user;

}

