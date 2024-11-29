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
@AllArgsConstructor // 빌더와 함께 사용할 모든 필드 생성자
public class Tag extends BaseTimeEntity {

    @Id
    @GeneratedValue
    @Column(name = "tag_id")
    private Long id;

    @NotBlank(message = "Tag name is required and cannot be blank.")
    @Size(min = 2, max = 50, message = "Tag name must be between 2 and 50 characters.")
    @Pattern(regexp = "^[a-zA-Z0-9\\s]+$", message = "Tag name can only contain letters, numbers, and spaces.")
    private String name;

    //연관관계
    @Builder.Default
    @OneToMany(mappedBy = "tag")
    private List<PostTag> postTags = new ArrayList<>();
}
