package com.github.jkky_98.noteJ.repository;

import com.github.jkky_98.noteJ.domain.Post;
import com.github.jkky_98.noteJ.domain.PostTag;
import com.github.jkky_98.noteJ.domain.Tag;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.web.config.CacheConfig;
import com.github.jkky_98.noteJ.web.controller.dto.TagCountDto;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({CacheConfig.class}) // 캐시 관련 빈이 필요하다면
@DisplayName("[UserRepository] Integration Tests")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager em;

    private User user;
    private Tag tag1;
    private Tag tag2;
    private Post post1; // writable = true
    private Post post2; // writable = false
    private PostTag pt1;
    private PostTag pt2;
    private PostTag pt3; // post2에 연결된 태그

    @BeforeEach
    void setUp() {
        // 사용자 생성 및 persist
        user = User.builder()
                .username("testUser")
                .email("testUser@test.com")
                .password("123456")
                .build();
        em.persist(user);

        // 태그 생성 및 persist
        tag1 = Tag.builder()
                .name("tag1")
                .build();
        tag2 = Tag.builder()
                .name("tag2")
                .build();
        em.persist(tag1);
        em.persist(tag2);

        // 게시글 생성
        post1 = Post.builder()
                .title("Post 1")
                .postUrl("url1")
                .writable(true)
                .user(user)
                .build();
        post2 = Post.builder()
                .title("Post 2")
                .postUrl("url2")
                .writable(false)
                .user(user)
                .build();
        em.persist(post1);
        em.persist(post2);

        // PostTag 생성: post1 (writable true) 연결 태그 : tag1, tag2
        pt1 = PostTag.builder()
                .post(post1)
                .tag(tag1)
                .build();
        pt2 = PostTag.builder()
                .post(post1)
                .tag(tag2)
                .build();
        em.persist(pt1);
        em.persist(pt2);

        // PostTag 생성: post2 (writable false) 연결: tag1 (이 경우는 집계 대상에서 제외됨)
        pt3 = PostTag.builder()
                .post(post2)
                .tag(tag1)
                .build();
        em.persist(pt3);

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("findByUsername() - 존재하는 사용자 조회")
    void testFindByUsername() {
        Optional<User> found = userRepository.findByUsername("testUser");
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("testUser@test.com");
    }

    @Test
    @DisplayName("findByEmail() - 존재하는 사용자 조회")
    void testFindByEmail() {
        Optional<User> found = userRepository.findByEmail("testUser@test.com");
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testUser");
    }

    @Test
    @DisplayName("findTagsByUser() - 사용자 태그 집계 조회 (writable 게시글만)")
    void testFindTagsByUser() {
        // when: user.id를 조건으로 호출하면, writable true인 post1의 태그만 집계됨.
        List<TagCountDto> tagCounts = userRepository.findTagsByUser(user.getId());

        // then: post1은 tag1, tag2가 각각 1회씩 등장해야 함.
        assertThat(tagCounts).hasSize(2);

        TagCountDto tag1Dto = tagCounts.stream()
                .filter(dto -> "tag1".equals(dto.getTagName()))
                .findFirst().orElse(null);
        TagCountDto tag2Dto = tagCounts.stream()
                .filter(dto -> "tag2".equals(dto.getTagName()))
                .findFirst().orElse(null);

        assertThat(tag1Dto).isNotNull();
        assertThat(tag1Dto.getCount()).isEqualTo(1);
        assertThat(tag2Dto).isNotNull();
        assertThat(tag2Dto.getCount()).isEqualTo(1);
    }
}
