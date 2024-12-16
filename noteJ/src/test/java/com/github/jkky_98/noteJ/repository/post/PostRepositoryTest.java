package com.github.jkky_98.noteJ.repository.post;

import com.github.jkky_98.noteJ.domain.*;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.repository.PostRepository;
import com.github.jkky_98.noteJ.repository.PostTagRepository;
import com.github.jkky_98.noteJ.repository.TagRepository;
import com.github.jkky_98.noteJ.repository.UserRepository;
import com.github.jkky_98.noteJ.web.controller.form.PostsConditionForm;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository; // User 저장용 Repository

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private PostTagRepository postTagRepository;

    @Autowired
    EntityManager em;

    private User testUser;

    private Post testPost;

    @BeforeEach
    void setUp() {
        // 유저 생성 및 저장
        testUser = User.builder()
                .username("testUser")
                .email("test@example.com")
                .password("password")
                .build();
        userRepository.save(testUser);

        // 게시글 생성 및 저장
        // 연관관계 매핑
        testPost = Post.builder()
                .title("Test Title")
                .content("Test Content is longer than 10 characters.")
                .postSummary("Summary of the post")
                .postUrl("test-url")
                .thumbnail("thumbnail.png")
                .writable(true)
                .user(testUser) // 연관관계 매핑
                .build();
        postRepository.save(testPost);
    }

    @Test
    @DisplayName("[PostRepository] findOnePost - 성공 테스트")
    void findPostByUsernameAndPostUrl_Success() {
        // when
        Post foundPost = postRepository.findOnePost("testUser", "test-url");

        // then
        assertThat(foundPost).isNotNull();
        assertThat(foundPost.getTitle()).isEqualTo("Test Title");
        assertThat(foundPost.getContent()).isEqualTo("Test Content is longer than 10 characters.");
        assertThat(foundPost.getPostUrl()).isEqualTo("test-url");
        assertThat(foundPost.getUser().getUsername()).isEqualTo("testUser");
    }

    @Test
    @DisplayName("[PostRepository] findOnePost - 실패 테스트 (존재하지 않는 username)")
    void findPostByUsernameAndPostUrl_Fail_Username() {
        // when
        Post foundPost = postRepository.findOnePost("nonexistentUser", "test-url");

        // then
        assertThat(foundPost).isNull();
    }

    @Test
    @DisplayName("[PostRepository] findOnePost - 실패 테스트 (존재하지 않는 postUrl)")
    void findPostByUsernameAndPostUrl_Fail_PostUrl() {
        // when
        Post foundPost = postRepository.findOnePost("testUser", "nonexistent-url");

        // then
        assertThat(foundPost).isNull();
    }

    @Test
    @DisplayName("[PostRepository] Post와 PostTag 연관관계 편의 메서드 테스트")
    void testPostAndPostTagRelationship() {
        // given
        Tag testTag = Tag.builder().name("Test Tag").build();
        PostTag postTag = PostTag.builder()
                .tag(testTag)
                .post(testPost)
                .build();

        // 연관관계 설정
        testPost.addPostTag(postTag);
        testTag.addPostTag(postTag);

        // when
        postTagRepository.save(postTag);
        tagRepository.save(testTag);
        postRepository.save(testPost);

        // then
        Post foundPost = postRepository.findById(testPost.getId()).orElseThrow();
        assertThat(foundPost.getPostTags()).hasSize(1);
        System.out.println("foundPost.getPostTags() = " + foundPost.getPostTags().get(0).getTag());
        assertThat(foundPost.getPostTags().get(0).getTag().getName()).isEqualTo("Test Tag");

        Tag foundTag = tagRepository.findById(testTag.getId()).orElseThrow();
        assertThat(foundTag.getPostTags()).hasSize(1);
        assertThat(foundTag.getPostTags().get(0).getPost().getTitle()).isEqualTo("Test Title");
    }

    @Test
    @DisplayName("[PostRepository] Post와 Comment 연관관계 편의 메서드 테스트")
    void testPostAndCommentRelationship() {
        // given
        Comment comment = Comment.builder()
                .content("Test Comment")
                .build();

        // 연관관계 설정
        testPost.addComment(comment);

        // when
        postRepository.save(testPost);

        // then
        Post foundPost = postRepository.findById(testPost.getId()).orElseThrow();
        assertThat(foundPost.getComments()).hasSize(1);
        assertThat(foundPost.getComments().get(0).getContent()).isEqualTo("Test Comment");
    }

    @Test
    @DisplayName("[PostRepository] Post와 Like 연관관계 편의 메서드 테스트")
    void testPostAndLikeRelationship() {
        // given
        Like like = Like.builder().build();

        // 연관관계 설정
        testPost.addLike(like);

        // when
        postRepository.save(testPost);

        // then
        Post foundPost = postRepository.findById(testPost.getId()).orElseThrow();
        assertThat(foundPost.getLikes()).hasSize(1);
    }

    @Test
    void searchPost_withVariousConditions() {
        // Given: 테스트 데이터 준비
        createTestData();

        // 1. Title 검색 테스트
        PostsConditionForm searchCondition1 = new PostsConditionForm();
        searchCondition1.setSearch("Querydsl");

        List<Post> result1 = postRepository.searchPost(searchCondition1);

        assertThat(result1).hasSize(1);
        assertThat(result1.get(0).getTitle()).isEqualTo("Querydsl Guide");

        // 2. TagName 검색 테스트
        PostsConditionForm searchCondition2 = new PostsConditionForm();
        searchCondition2.setTagName("Java");

        List<Post> result2 = postRepository.searchPost(searchCondition2);
        assertThat(result2).hasSize(1);
        assertThat(result2.get(0).getTitle()).isEqualTo("Spring Boot Guide");

        // 3. SeriesName 검색 테스트
        PostsConditionForm searchCondition3 = new PostsConditionForm();
        searchCondition3.setSeriesName("Backend Development");

        List<Post> result3 = postRepository.searchPost(searchCondition3);
        assertThat(result3).hasSize(2);

        // 4. Title + TagName 복합 검색 테스트
        PostsConditionForm searchCondition4 = new PostsConditionForm();
        searchCondition4.setSearch("Spring");
        searchCondition4.setTagName("Java");

        List<Post> result4 = postRepository.searchPost(searchCondition4);
        assertThat(result4).hasSize(1);
        assertThat(result4.get(0).getTitle()).isEqualTo("Spring Boot Guide");
    }

    private void createTestData() {
        // Series
        Series backendSeries = Series.builder()
                .seriesName("Backend Development").build();
        Series frontendSeries = Series.builder()
                .seriesName("Frontend Development").build();

        em.persist(backendSeries);
        em.persist(frontendSeries);

        // Tags
        Tag javaTag = Tag.builder()
                .name("Java").build();
        Tag reactTag = Tag.builder()
                .name("React").build();
        em.persist(javaTag);
        em.persist(reactTag);

        // Posts
        Post post1 = Post.builder()
                .title("Spring Boot Guide")
                .content("Spring Boot content")
                .series(backendSeries)
                .build();
        Post post2 = Post.builder()
                .title("Querydsl Guide")
                .content("Querydsl content")
                .series(backendSeries)
                .build();
        Post post3 = Post.builder()
                .title("React Basics")
                .content("React content")
                .series(frontendSeries)
                .build();

        em.persist(post1);
        em.persist(post2);
        em.persist(post3);

        // PostTags
        PostTag postTag1 = PostTag.builder().post(post1).tag(javaTag).build();
        PostTag postTag2 = PostTag.builder().post(post3).tag(reactTag).build();
        em.persist(postTag1);
        em.persist(postTag2);

        em.flush();
        em.clear();
    }
}
