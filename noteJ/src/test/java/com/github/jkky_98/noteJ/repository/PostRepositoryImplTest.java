package com.github.jkky_98.noteJ.repository;

import com.github.jkky_98.noteJ.domain.Post;
import com.github.jkky_98.noteJ.domain.PostTag;
import com.github.jkky_98.noteJ.domain.Series;
import com.github.jkky_98.noteJ.domain.Tag;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.repository.post.PostRepositoryImpl;
import com.github.jkky_98.noteJ.web.config.CacheConfig;
import com.github.jkky_98.noteJ.web.controller.form.PostsConditionForm;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({CacheConfig.class})
@DisplayName("[PostRepositoryImpl] Integration Tests")
public class PostRepositoryImplTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private PostRepository postRepository;

    // 샘플 엔티티들
    private User user;
    private Post post;
    private Series series;
    private Tag tag;
    private PostTag postTag;

    @BeforeEach
    void setUp() {
        // 샘플 사용자 생성
        user = User.builder()
                .username("testUser")
                .email("testUser@gmail.com")
                .password("123456")
                .build();

        em.persist(user);

        // 샘플 시리즈 생성
        series = Series.builder()
                .seriesName("Test Series")
                .build();

        em.persist(series);

        // 샘플 게시글 생성 (writable=true로 설정)

        post = Post.builder()
                .title("Test Post Title")
                .writable(true)
                .user(user)
                .postUrl("fgisoda30-dmfklas0-fmklasd")
                .series(series)
                .build();

        em.persist(post);

        // 샘플 태그 생성
        tag = Tag.builder()
                .name("testTag")
                .build();

        em.persist(tag);

        // 게시글에 태그 연결 (PostTag)
        postTag = PostTag.builder()
                .post(post)
                .tag(tag)
                .build();

        em.persist(postTag);

        post.getPostTags().add(postTag);

        // 변경 사항 flush 및 clear
        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("searchPosts() - 필터 조건에 따른 포스트 검색")
    void testSearchPosts() {
        // given: PostsConditionForm에 검색 조건 설정
        PostsConditionForm form = new PostsConditionForm();
        form.setSearch("Test");          // title에 "Test"가 포함
        form.setTagName("testTag");      // 태그명 "testTag"
        form.setSeriesName("Test Series"); // 시리즈명 "Test Series"

        // when: 검색 실행 (user.getId()를 검색 조건으로 사용)
        List<Post> result = postRepository.searchPosts(form, user.getId());

        // then: 결과가 1건 이상이어야 함 (setUp()에서 1개의 포스트를 등록했으므로)
        assertThat(result)
                .isNotEmpty()
                .allMatch(p -> p.getTitle().contains("Test"));
    }

    @Test
    @DisplayName("searchPostsWithPage() - 페이징 적용 포스트 검색")
    void testSearchPostsWithPage() {
        // given: 검색 조건 설정 (조건에 맞는 포스트 1건 존재)
        PostsConditionForm form = new PostsConditionForm();
        form.setSearch("Test");
        form.setTagName("testTag");
        form.setSeriesName("Test Series");

        PageRequest pageable = PageRequest.of(0, 5);

        // when: 페이징 검색 실행
        Page<Post> pageResult = postRepository.searchPostsWithPage(form, user.getId(), pageable);

        // then: 페이지 내용과 전체 개수 검증
        assertThat(pageResult.getContent()).isNotEmpty();
        assertThat(pageResult.getTotalElements()).isGreaterThan(0);
    }

    @Test
    @DisplayName("searchPosts: 검색 조건이 모두 null인 경우 (전체 포스트 검색)")
    void testSearchPosts_allNullConditions() {
        // given: 모든 검색 조건을 null로 설정
        PostsConditionForm form = new PostsConditionForm();
        form.setSearch(null);
        form.setTagName(null);
        form.setSeriesName(null);

        // when: 검색 실행
        List<Post> result = postRepository.searchPosts(form, user.getId());

        // then: 등록된 모든 게시글(이 예제에서는 1건)이 반환되어야 함
        assertThat(result)
                .isNotEmpty()
                .allMatch(p -> p.getUser().getId().equals(user.getId()));
    }

    @Test
    @DisplayName("searchPosts: 검색 조건이 빈 문자열인 경우 (전체 포스트 검색)")
    void testSearchPosts_emptyConditions() {
        // given: 모든 검색 조건을 빈 문자열로 설정
        PostsConditionForm form = new PostsConditionForm();
        form.setSearch("");
        form.setTagName("");
        form.setSeriesName("");

        // when: 검색 실행
        List<Post> result = postRepository.searchPosts(form, user.getId());

        // then: 등록된 모든 게시글이 반환되어야 함
        assertThat(result)
                .isNotEmpty()
                .allMatch(p -> p.getUser().getId().equals(user.getId()));
    }

    @Test
    @DisplayName("searchPosts: 조건에 맞는 게시글이 없는 경우")
    void testSearchPosts_noMatch() {
        // given: 존재하지 않는 태그명 또는 시리즈명을 조건으로 설정
        PostsConditionForm form = new PostsConditionForm();
        form.setSearch("NonExistingTitle");
        form.setTagName("NonExistingTag");
        form.setSeriesName("NonExistingSeries");

        // when: 검색 실행
        List<Post> result = postRepository.searchPosts(form, user.getId());

        // then: 결과는 빈 리스트여야 함
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("searchPostsWithPage: 경계 조건 - 검색 결과가 없는 경우")
    void testSearchPostsWithPage_noMatch() {
        // given: 조건에 맞는 게시글이 없는 검색 조건 설정
        PostsConditionForm form = new PostsConditionForm();
        form.setSearch("NonExisting");
        form.setTagName("NonExistingTag");
        form.setSeriesName("NonExistingSeries");

        PageRequest pageable = PageRequest.of(0, 5);

        // when: 페이징 검색 실행
        Page<Post> pageResult = postRepository.searchPostsWithPage(form, user.getId(), pageable);

        // then: 결과 페이지의 내용은 빈 리스트여야 함, 전체 개수 0
        assertThat(pageResult.getContent()).isEmpty();
        assertThat(pageResult.getTotalElements()).isEqualTo(0);
    }
}
