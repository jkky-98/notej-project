package com.github.jkky_98.noteJ.domain;

import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.domain.user.UserDesc;
import com.github.jkky_98.noteJ.domain.user.UserRole;
import com.github.jkky_98.noteJ.web.controller.dto.AutoEditPostRequest;
import com.github.jkky_98.noteJ.web.controller.form.WriteForm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.*;

public class PostTest {
    @Test
    @DisplayName("[Post] 빌더를 통한 객체 생성 테스트")
    void postBuilderCreationTest() {
        // given: 테스트용 User와 Series 객체 생성
        User user = createTestUser("testuser", "test@example.com");
        Series series = createTestSeries("Test Series");

        // when: 빌더를 사용하여 모든 필드를 명시적으로 설정하여 Post 객체 생성
        Post post = Post.builder()
                .title("Sample Title")
                .content("Sample Content")
                .postSummary("Sample Summary")
                .postUrl("sample-url")
                .thumbnail("thumb.jpg")
                .writable(true)
                .viewCount(100)
                .user(user)
                .series(series)
                .build();

        // then: 각 필드의 값이 올바르게 설정되었는지 검증
        assertThat(post.getTitle()).isEqualTo("Sample Title");
        assertThat(post.getContent()).isEqualTo("Sample Content");
        assertThat(post.getPostSummary()).isEqualTo("Sample Summary");
        assertThat(post.getPostUrl()).isEqualTo("sample-url");
        assertThat(post.getThumbnail()).isEqualTo("thumb.jpg");
        assertThat(post.getWritable()).isTrue();
        assertThat(post.getViewCount()).isEqualTo(100);
        assertThat(post.getUser()).isEqualTo(user);
        assertThat(post.getSeries()).isEqualTo(series);
    }

    @Test
    @DisplayName("[Post] 기본 상태 테스트")
    void postDefaultStateTest() {
        Post post = Post.builder().build();

        // ID는 영속화 전이므로 null이어야 함
        assertThat(post.getId()).isNull();

        // 기본 필드 값은 설정하지 않았으므로 null
        assertThat(post.getTitle()).isNull();
        assertThat(post.getContent()).isNull();
        assertThat(post.getPostSummary()).isNull();
        assertThat(post.getPostUrl()).isNull();
        assertThat(post.getThumbnail()).isNull();
        assertThat(post.getWritable()).isNull();

        // 버전은 null, viewCount는 기본 0
        assertThat(post.getVersion()).isNull();
        assertThat(post.getViewCount()).isEqualTo(0);

        // 연관관계 필드는 null
        assertThat(post.getSeries()).isNull();
        assertThat(post.getUser()).isNull();

        // @Builder.Default 컬렉션들은 null이 아니며 빈 리스트여야 함
        assertThat(post.getComments()).isNotNull().isEmpty();
        assertThat(post.getLikes()).isNotNull().isEmpty();
        assertThat(post.getPostTags()).isNotNull().isEmpty();
        assertThat(post.getPostFiles()).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("[Post] updateUser 메서드 테스트")
    void updateUserTest() {
        User user1 = createTestUser("user1", "user1@example.com");
        User user2 = createTestUser("user2", "user2@example.com");
        Post post = createTestPost(user1);

        post.updateUser(user2);

        assertThat(post.getUser()).isEqualTo(user2);
    }

    @Test
    @DisplayName("[Post] addPostTag / removePostTag 메서드 테스트")
    void postTagTest() {
        User user = createTestUser("user", "user@example.com");
        Post post = createTestPost(user);
        PostTag tag = createTestPostTag(post, "testTag");

        // addPostTag
        assertThat(post.getPostTags()).isEmpty();
        post.addPostTag(tag);
        assertThat(post.getPostTags()).contains(tag);
        // 연관관계 편의 메서드를 통해 tag의 post가 설정되어야 함
        assertThat(tag.getPost()).isEqualTo(post);

        // removePostTag
        post.removePostTag(tag);
        assertThat(post.getPostTags()).doesNotContain(tag);
        assertThat(tag.getPost()).isNull();
    }

    @Test
    @DisplayName("[Post] addComment / removeComment 메서드 테스트")
    void commentTest() {
        User user = createTestUser("user", "user@example.com");
        Post post = createTestPost(user);
        Comment comment = createTestComment("Nice post!");

        // addComment
        assertThat(post.getComments()).isEmpty();
        post.addComment(comment);
        assertThat(post.getComments()).contains(comment);
        assertThat(comment.getPost()).isEqualTo(post);

        // removeComment
        post.removeComment(comment);
        assertThat(post.getComments()).doesNotContain(comment);
        assertThat(comment.getPost()).isNull();
    }

    @Test
    @DisplayName("[Post] addLike / removeLike 메서드 테스트")
    void likeTest() {
        User user = createTestUser("user", "user@example.com");
        User userGetLike = createTestUser("userGetLike", "userGetLike@example.com");
        Post post = createTestPost(user);
        Like like = Like.builder()
                .user(user)
                .userGetLike(userGetLike)
                .build();

        // addLike
        assertThat(post.getLikes()).isEmpty();
        post.addLike(like);
        assertThat(post.getLikes()).contains(like);
        assertThat(like.getPost()).isEqualTo(post);

        // removeLike
        post.removeLike(like);
        assertThat(post.getLikes()).doesNotContain(like);
        assertThat(like.getPost()).isNull();
    }

    @Test
    @DisplayName("[Post] updateSeries 메서드 테스트")
    void updateSeriesTest() {
        User user = createTestUser("user", "user@example.com");
        Post post = createTestPost(user);
        Series series = createTestSeries("Test Series");

        assertThat(post.getSeries()).isNull();
        post.updateSeries(series);
        assertThat(post.getSeries()).isEqualTo(series);
        assertThat(series.getPosts()).contains(post);
    }

    // 7. increaseViewCount 메서드 테스트
    @Test
    @DisplayName("[Post] increaseViewCount 메서드 테스트")
    void increaseViewCountTest() {
        User user = createTestUser("user", "user@example.com");
        Post post = createTestPost(user);
        int initialCount = post.getViewCount();

        post.increaseViewCount();

        assertThat(post.getViewCount()).isEqualTo(initialCount + 1);
    }

    @Test
    @DisplayName("[Post] updatePostWithoutThumbnailAndSeries 메서드 테스트")
    void updatePostWithoutThumbnailAndSeriesTest() {
        User user = createTestUser("user", "user@example.com");
        Post post = createTestPost(user);
        WriteForm form = WriteForm.builder()
                .title("Updated Title")
                .content("Updated Content")
                .postSummary("Updated Summary")
                .open(true)
                .build();

        post.updatePostWithoutThumbnailAndSeries(form);

        assertThat(post.getTitle()).isEqualTo("Updated Title");
        assertThat(post.getContent()).isEqualTo("Updated Content");
        assertThat(post.getPostSummary()).isEqualTo("Updated Summary");
        assertThat(post.getWritable()).isEqualTo(true);
    }

    @Test
    @DisplayName("[Post] updateThumbnail 메서드 테스트")
    void updateThumbnailTest() {
        User user = createTestUser("user", "user@example.com");
        Post post = createTestPost(user);

        post.updateThumbnail("newThumbnail.jpg");

        assertThat(post.getThumbnail()).isEqualTo("newThumbnail.jpg");
    }

    @Test
    @DisplayName("[Post] updateEditPostTemp 메서드 테스트")
    void updateEditPostTempTest() {
        User user = createTestUser("user", "user@example.com");
        Post post = createTestPost(user);
        Series series = createTestSeries("Test Series");

        // AutoEditPostRequest: 제목과 인코딩된 컨텐츠를 전달한다고 가정
        AutoEditPostRequest request = new AutoEditPostRequest();
        request.setTitle("Edited Title");
        request.setContent("Edited%20Content%20with%20encoding");

        post.updateEditPostTemp(request, series);

        // 제목은 업데이트되어야 함
        assertThat(post.getTitle()).isEqualTo("Edited Title");
        // 컨텐츠는 URL 디코딩되어 업데이트되어야 함
        String expectedContent = URLDecoder.decode("Edited%20Content%20with%20encoding", StandardCharsets.UTF_8);
        assertThat(post.getContent()).isEqualTo(expectedContent);
        // postUrl는 제목 + UUID 형식이므로 제목으로 시작하는지 확인
        assertThat(post.getPostUrl()).startsWith("Edited Title");
        // 연관된 시리즈가 업데이트되어야 함
        assertThat(post.getSeries()).isEqualTo(series);
    }

    // 헬퍼 메서드: 테스트용 User 객체 생성
    private User createTestUser(String username, String email) {
        UserDesc userDesc = UserDesc.builder()
                .description("Test user description")
                .blogTitle("Test Blog")
                .build();

        return User.builder()
                .username(username)
                .email(email)
                .password("password123")
                .userRole(UserRole.USER)
                .userDesc(userDesc)
                .build();
    }

    // 헬퍼 메서드: 테스트용 Post 객체 생성
    private Post createTestPost(User user) {
        return Post.builder()
                .title("Test Post Title")
                .content("Test Post Content")
                .postSummary("Test Summary")
                .writable(true)
                .viewCount(0)
                .user(user)
                .build();
    }

    // 헬퍼 메서드: 테스트용 Series 객체 생성
    private Series createTestSeries(String seriesName) {
        return Series.builder()
                .seriesName(seriesName)
                .build();
    }

    // 헬퍼 메서드: 테스트용 PostTag 객체 생성
    private PostTag createTestPostTag(Post post, String tagName) {
        Tag tag = Tag.builder()
                .name(tagName)
                .build();
        return PostTag.builder()
                .tag(tag)
                .post(post)
                .build();
    }

    // 헬퍼 메서드: 테스트용 Comment 객체 생성
    private Comment createTestComment(String content) {
        return Comment.builder()
                .content(content)
                .build();
    }
}

