package com.github.jkky_98.noteJ.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PostTest {

    @Test
    @DisplayName("[Post] 빌더를 통한 객체 생성 테스트")
    void postBuilderTest() {
        // given
        String title = "Test Title";
        String content = "This is the content of the post.";
        String thumbnail = "https://example.com/thumbnail.jpg";
        Boolean isPrivate = true;

        Post post = Post.builder()
                .title(title)
                .content(content)
                .thumbnail(thumbnail)
                .writable(isPrivate)
                .build();

        // then
        assertThat(post).isNotNull();
        assertThat(post.getTitle()).isEqualTo(title);
        assertThat(post.getContent()).isEqualTo(content);
        assertThat(post.getThumbnail()).isEqualTo(thumbnail);
        assertThat(post.getWritable()).isEqualTo(isPrivate);
        assertThat(post.getSeries()).isNull(); // 연관관계는 설정하지 않았으므로 null
        assertThat(post.getUser()).isNull();
    }

    @Test
    @DisplayName("[Post] 기본 상태 테스트")
    void postDefaultStateTest() {
        // given
        Post post = Post.builder().build();

        // then
        assertThat(post).isNotNull();
        assertThat(post.getTitle()).isNull();
        assertThat(post.getContent()).isNull();
        assertThat(post.getThumbnail()).isNull();
        assertThat(post.getWritable()).isNull();
        assertThat(post.getSeries()).isNull();
        assertThat(post.getUser()).isNull();
        assertThat(post.getComments()).isNotNull();
        assertThat(post.getLikes()).isNotNull();
        assertThat(post.getPostTags()).isNotNull();
    }

    @Test
    @DisplayName("[Post] @Builder.Default 초기화 테스트")
    void builderDefaultInitializationTest() {
        // given
        Post post = Post.builder().build();

        // then
        assertThat(post.getComments()).isNotNull();
        assertThat(post.getComments()).isEmpty();

        assertThat(post.getLikes()).isNotNull();
        assertThat(post.getLikes()).isEmpty();

        assertThat(post.getPostTags()).isNotNull();
        assertThat(post.getPostTags()).isEmpty();
    }

    @Test
    @DisplayName("[Post] addComment 메서드 테스트")
    void addCommentTest() {
        // given
        Post post = Post.builder()
                .title("Test Post")
                .content("This is a test post")
                .build();

        Comment comment = Comment.builder()
                .content("This is a test comment")
                .build();

        // when
        post.addComment(comment);

        // then
        // comments 리스트에 comment가 추가되었는지 확인
        assertThat(post.getComments()).containsExactly(comment);

        // comment의 post가 설정되었는지 확인
        assertThat(comment.getPost()).isEqualTo(post);
    }

    @Test
    @DisplayName("[Post] removeComment 메서드 테스트")
    void removeCommentTest() {
        // given
        Post post = Post.builder()
                .title("Test Post")
                .content("This is a test post")
                .build();

        Comment comment = Comment.builder()
                .content("This is a test comment")
                .build();

        // Post 객체에 Comment 추가
        post.addComment(comment);

        // when
        post.removeComment(comment);

        // then
        // comments 리스트에서 comment가 제거되었는지 확인
        assertThat(post.getComments()).doesNotContain(comment);

        // comment의 post 필드가 null로 설정되었는지 확인
        assertThat(comment.getPost()).isNull();
    }

    @Test
    @DisplayName("[Post] addPostTag 메서드 테스트")
    void addPostTagTest() {
        // given
        Post post = Post.builder()
                .title("Test Post")
                .content("This is a test post")
                .build();

        PostTag postTag = PostTag.builder()
                .build();

        // when
        post.addPostTag(postTag);

        // then
        // postTags 리스트에 postTag가 추가되었는지 확인
        assertThat(post.getPostTags()).containsExactly(postTag);

        // postTag의 post 필드가 post로 설정되었는지 확인
        assertThat(postTag.getPost()).isEqualTo(post);
    }

    @Test
    @DisplayName("[Post] removePostTag 메서드 테스트")
    void removePostTag_shouldRemovePostTagFromPostTagsListAndUpdatePostTagPost() {
        // given
        Post post = Post.builder()
                .title("Test Post")
                .content("This is a test post")
                .build();

        PostTag postTag = PostTag.builder()
                .build();

        // Post 객체에 PostTag 추가
        post.addPostTag(postTag);

        // when
        post.removePostTag(postTag);

        // then
        // postTags 리스트에서 postTag가 제거되었는지 확인
        assertThat(post.getPostTags()).doesNotContain(postTag);

        // postTag의 post 필드가 null로 설정되었는지 확인
        assertThat(postTag.getPost()).isNull();
    }

    @Test
    @DisplayName("[Post] addLike 메서드 테스트")
    void addLikeTest() {
        // given
        Post post = Post.builder()
                .title("Test Post")
                .content("This is a test post")
                .build();

        Like like = Like.builder()
                .build();

        // when
        post.addLike(like);

        // then
        // likes 리스트에 like가 추가되었는지 확인
        assertThat(post.getLikes()).containsExactly(like);

        // like의 post 필드가 post로 설정되었는지 확인
        assertThat(like.getPost()).isEqualTo(post);
    }

    @Test
    @DisplayName("[Post] removeLike 메서드 테스트")
    void removeLikeTest() {
        // given
        Post post = Post.builder()
                .title("Test Post")
                .content("This is a test post")
                .build();

        Like like = Like.builder()
                .build();

        // Post 객체에 Like 추가
        post.addLike(like);

        // when
        post.removeLike(like);

        // then
        // likes 리스트에서 like가 제거되었는지 확인
        assertThat(post.getLikes()).doesNotContain(like);

        // like의 post 필드가 null로 설정되었는지 확인
        assertThat(like.getPost()).isNull();
    }
}
