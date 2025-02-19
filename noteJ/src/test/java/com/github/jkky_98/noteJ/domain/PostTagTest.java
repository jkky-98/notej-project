package com.github.jkky_98.noteJ.domain;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PostTagTest {

    @Test
    @DisplayName("[PostTag] 빌더를 통한 객체 생성 테스트")
    void postTagBuilderTest() {
        Post post = createTestPost("Test Post", "This is a test post.");
        Tag tag = createTestTag("Test Tag");
        PostTag postTag = createTestPostTag(post, tag);

        assertThat(postTag).isNotNull();
        assertThat(postTag.getPost()).isEqualTo(post);
        assertThat(postTag.getTag()).isEqualTo(tag);
    }

    @Test
    @DisplayName("[PostTag] 기본 상태 테스트")
    void postTagDefaultStateTest() {
        PostTag defaultPostTag = PostTag.builder().build();

        assertThat(defaultPostTag).isNotNull();
        assertThat(defaultPostTag.getPost()).isNull();
        assertThat(defaultPostTag.getTag()).isNull();
    }

    @Test
    @DisplayName("[PostTag] updateTag 메서드 테스트")
    void updateTagTest() {
        Post post = createTestPost("Test Post", "This is a test post.");
        Tag initialTag = createTestTag("Initial Tag");
        Tag newTag = createTestTag("New Tag");
        PostTag postTag = createTestPostTag(post, initialTag);

        postTag.updateTag(newTag);
        assertThat(postTag.getTag()).isEqualTo(newTag);
    }

    @Test
    @DisplayName("[PostTag] updatePost 메서드 테스트")
    void updatePostTest() {
        Post oldPost = createTestPost("Old Post", "Old Content");
        Post newPost = createTestPost("New Post", "New Content");
        Tag tag = createTestTag("Test Tag");
        PostTag postTag = createTestPostTag(oldPost, tag);

        postTag.updatePost(newPost);
        assertThat(postTag.getPost()).isEqualTo(newPost);
    }

    // 헬퍼 메서드: 테스트용 Post 객체 생성
    private Post createTestPost(String title, String content) {
        return Post.builder()
                .title(title)
                .content(content)
                .build();
    }

    // 헬퍼 메서드: 테스트용 Tag 객체 생성
    private Tag createTestTag(String name) {
        return Tag.builder()
                .name(name)
                .build();
    }

    // 헬퍼 메서드: 테스트용 PostTag 객체 생성 (Post와 Tag를 인자로 받음)
    private PostTag createTestPostTag(Post post, Tag tag) {
        return PostTag.builder()
                .post(post)
                .tag(tag)
                .build();
    }
}