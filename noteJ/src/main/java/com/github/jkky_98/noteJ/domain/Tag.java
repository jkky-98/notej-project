package com.github.jkky_98.noteJ.domain;

import com.github.jkky_98.noteJ.domain.base.BaseTimeEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 기본 생성자
@AllArgsConstructor(access = AccessLevel.PRIVATE) // 빌더와 함께 사용할 모든 필드 생성자
public class Tag extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id", nullable = false)
    private Long id;

    @Column(nullable = false, length = 20)
    private String name;

    //연관관계
    @Builder.Default
    @OneToMany(mappedBy = "tag", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostTag> postTags = new ArrayList<>();

    // 연관 관계 편의 메서드
    public void addPostTag(PostTag postTag) {
        this.postTags.add(postTag);
        postTag.updateTag(this); // 주인 쪽에도 관계 설정
    }

    public void removePostTag(PostTag postTag) {
        this.postTags.remove(postTag);
        postTag.updateTag(null); // 주인 쪽 관계 해제
    }

    public static Tag of(String tagName) {
        return Tag.builder()
                .name(tagName)
                .build();
    }
}
