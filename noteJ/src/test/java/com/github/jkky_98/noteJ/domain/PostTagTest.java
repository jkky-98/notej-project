package com.github.jkky_98.noteJ.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
class PostTagTest {

    private Post post;
    private Post oldPost;
    private Post newPost;
    private Tag tag;
    private Tag oldTag;
    private Tag newTag;
    private PostTag postTag;

    @BeforeEach
    void setup() {
        // 공통 객체 초기화
        post = Post.builder()
                .title("Test Post")
                .content("This is a test post.")
                .build();

        oldPost = Post.builder()
                .title("Old Post")
                .content("Old Content")
                .build();

        newPost = Post.builder()
                .title("New Post")
                .content("New Content")
                .build();

        tag = Tag.builder()
                .name("Test Tag")
                .build();

        oldTag = Tag.builder()
                .name("Old Tag")
                .build();

        newTag = Tag.builder()
                .name("New Tag")
                .build();

        postTag = PostTag.builder()
                .post(post)
                .tag(tag)
                .build();
    }

    @Test
    @DisplayName("[PostTag] 빌더를 통한 객체 생성 테스트")
    void postTagBuilderTest() {
        // then
        assertThat(postTag).isNotNull();
        assertThat(postTag.getPost()).isEqualTo(post);
        assertThat(postTag.getTag()).isEqualTo(tag);
    }

    @Test
    @DisplayName("[PostTag] 기본 상태 테스트")
    void postTagDefaultStateTest() {
        // given
        PostTag defaultPostTag = PostTag.builder().build();

        // then
        assertThat(defaultPostTag).isNotNull();
        assertThat(defaultPostTag.getPost()).isNull();
        assertThat(defaultPostTag.getTag()).isNull();
    }

    @Test
    @DisplayName("[PostTag] updateTag 메서드 테스트")
    void updateTagTest() {
        // when
        postTag.updateTag(newTag);

        // then
        // postTag의 tag 필드가 newTag로 업데이트되었는지 확인
        assertThat(postTag.getTag()).isEqualTo(newTag);
    }

    @Test
    @DisplayName("[PostTag] updatePost 메서드 테스트")
    void updatePostTest() {
        // given
        PostTag postTagWithOldPost = PostTag.builder()
                .post(oldPost)
                .tag(tag)
                .build();

        // when
        postTagWithOldPost.updatePost(newPost);

        // then
        // postTag의 post 필드가 newPost로 업데이트되었는지 확인
        assertThat(postTagWithOldPost.getPost()).isEqualTo(newPost);
    }
}