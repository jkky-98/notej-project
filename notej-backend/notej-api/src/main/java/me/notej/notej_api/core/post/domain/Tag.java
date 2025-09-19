package me.notej.notej_api.core.post.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tag")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private Long id;

    @Column(nullable = false, length = 20)
    private String name;

    @OneToMany(mappedBy = "tag")
    @Builder.Default
    private List<PostTag> postTags = new ArrayList<>();

    public void addPostTag(PostTag postTag) {
        this.postTags.add(postTag);
        postTag.setTag(this);
    }
}
