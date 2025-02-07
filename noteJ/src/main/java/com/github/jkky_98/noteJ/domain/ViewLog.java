package com.github.jkky_98.noteJ.domain;

import com.github.jkky_98.noteJ.domain.base.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 기본 생성자
@AllArgsConstructor(access = AccessLevel.PRIVATE) // 빌더와 함께 사용할 모든 필드 생성자
public class ViewLog extends BaseTimeEntity {

    @Id
    @GeneratedValue
    @Column(name = "view_log_id")
    private Long id;

    private String identifier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    public boolean isExpired() {
        return this.getCreateDt().isBefore(LocalDateTime.now().minusHours(24));
    }
}
