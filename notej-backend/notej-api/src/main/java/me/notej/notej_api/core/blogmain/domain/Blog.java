package me.notej.notej_api.core.blogmain.domain;

import jakarta.persistence.*;
import lombok.*;
import me.notej.notej_api.global.baseentity.BaseTimeEntity;

@Entity
@Table(name="blog")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Blog extends BaseTimeEntity {

    @Id
    @Column(name = "blog_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    private String title;

    @Column(unique = true)
    private String url;

    private String bio;

    private String profileImage;
}
