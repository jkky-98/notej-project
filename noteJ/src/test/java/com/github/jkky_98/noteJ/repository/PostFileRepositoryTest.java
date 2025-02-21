package com.github.jkky_98.noteJ.repository;

import com.github.jkky_98.noteJ.domain.Post;
import com.github.jkky_98.noteJ.domain.PostFile;
import com.github.jkky_98.noteJ.web.config.CacheConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({CacheConfig.class})
@DisplayName("[PostFileRepository] Integration Tests")
class PostFileRepositoryTest {

    @Autowired
    private PostFileRepository postFileRepository;

    @Autowired
    private EntityManager em;

    private PostFile postFile1;
    private PostFile postFile2;
    private Post post;
    private Long postId;

    @BeforeEach
    void setUp() {
        // 동일한 postId를 가진 PostFile 엔티티 2건 생성
        post = Post.builder()
                .postUrl("unique-post-url")
                .build();

        em.persist(post);

        postId = post.getId();

        postFile1 = PostFile.builder()
                .post(post)
                .url("file1.jpg")
                .build();
        postFile2 = PostFile.builder()
                .post(post)
                .url("file2.jpg")
                .build();

        em.persist(postFile1);
        em.persist(postFile2);
        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("deleteByPostId() - 지정한 postId의 파일들 삭제")
    void testDeleteByPostId() {
        // when
        postFileRepository.deleteByPostId(postId);
        em.flush();
        em.clear();

        // then: postId에 해당하는 PostFile이 존재하지 않아야 함
        List<PostFile> remaining = em.createQuery("SELECT pf FROM PostFile pf WHERE pf.id = :postId", PostFile.class)
                .setParameter("postId", postId)
                .getResultList();
        assertThat(remaining).isEmpty();
    }

    @Test
    @DisplayName("deleteByPostId() - 특정 포스트에 연결된 파일만 삭제되고 다른 포스트의 파일은 남음")
    void testDeleteByPostId_onlyDeletesSpecifiedPostFiles() {
        // given: 새로운 Post 및 PostFile 생성 (두 포스트 모두 존재)
        Post secondPost = Post.builder()
                .postUrl("second-post-url")
                .build();
        em.persist(secondPost);
        em.flush();
        // 재조회하여 관리 상태로 만듦
        secondPost = em.find(Post.class, secondPost.getId());
        Long secondPostId = secondPost.getId();

        PostFile secondPostFile = PostFile.builder()
                .post(secondPost)
                .url("secondFile.jpg")
                .build();
        em.persist(secondPostFile);
        em.flush();
        em.clear();

        // when: 첫 번째 post에 연결된 파일들만 삭제
        postFileRepository.deleteByPostId(postId);
        em.flush();
        em.clear();

        // then:
        // 첫 번째 post에 연결된 파일은 삭제되어야 함
        List<PostFile> remainingFirst = em.createQuery("SELECT pf FROM PostFile pf WHERE pf.post.id = :postId", PostFile.class)
                .setParameter("postId", postId)
                .getResultList();
        // 두 번째 post에 연결된 파일은 남아있어야 함
        List<PostFile> remainingSecond = em.createQuery("SELECT pf FROM PostFile pf WHERE pf.post.id = :postId", PostFile.class)
                .setParameter("postId", secondPostId)
                .getResultList();

        assertThat(remainingFirst).isEmpty();
        assertThat(remainingSecond).hasSize(1);
    }
}
