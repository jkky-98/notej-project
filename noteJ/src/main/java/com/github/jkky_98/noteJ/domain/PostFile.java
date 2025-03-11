package com.github.jkky_98.noteJ.domain;

import com.github.jkky_98.noteJ.domain.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 기본 생성자
@AllArgsConstructor(access = AccessLevel.PRIVATE) // 빌더와 함께 사용할 모든 필드 생성자
public class PostFile extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_file_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @Column(nullable = false)
    private String url;
}
