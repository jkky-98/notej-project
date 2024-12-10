package com.github.jkky_98.noteJ.repository.like;

import com.github.jkky_98.noteJ.domain.Like;
import com.github.jkky_98.noteJ.domain.Post;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.repository.LikeRepository;
import com.github.jkky_98.noteJ.repository.PostRepository;
import com.github.jkky_98.noteJ.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class LikeRepositoryTest {

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;
    private Post post;

    @BeforeEach
    void setup() {
        // 공통적으로 사용하는 User와 Post 저장
        user = userRepository.save(User.builder().username("TestUser").build());
        post = postRepository.save(Post.builder().title("Test Post").content("Test Content").build());
    }

    @Test
    @DisplayName("[LikeRepository] 좋아요 저장 테스트")
    void saveLikeTest() {
        // given
        Like like = Like.builder()
                .user(user)
                .post(post)
                .build();

        // when
        Like savedLike = likeRepository.save(like);

        // then
        assertThat(savedLike.getId()).isNotNull();
        assertThat(savedLike.getUser()).isEqualTo(user);
        assertThat(savedLike.getPost()).isEqualTo(post);
    }

    @Test
    @DisplayName("[LikeRepository] 좋아요 조회 테스트")
    void findLikeByIdTest() {
        // given
        Like like = likeRepository.save(Like.builder()
                .user(user)
                .post(post)
                .build());

        // when
        Optional<Like> foundLike = likeRepository.findById(like.getId());

        // then
        assertThat(foundLike).isPresent();
        assertThat(foundLike.get().getUser()).isEqualTo(user);
        assertThat(foundLike.get().getPost()).isEqualTo(post);
    }

    @Test
    @DisplayName("[LikeRepository] 좋아요 삭제 테스트")
    void deleteLikeTest() {
        // given
        Like like = likeRepository.save(Like.builder()
                .user(user)
                .post(post)
                .build());

        // when
        likeRepository.delete(like);

        // then
        Optional<Like> deletedLike = likeRepository.findById(like.getId());
        assertThat(deletedLike).isEmpty();
    }

    @Test
    @DisplayName("[LikeRepository] 특정 게시물의 좋아요 조회 테스트")
    void findAllByPostTest() {
        // given
        User user1 = userRepository.save(User.builder().username("User1").build());
        User user2 = userRepository.save(User.builder().username("User2").build());

        likeRepository.save(Like.builder().user(user1).post(post).build());
        likeRepository.save(Like.builder().user(user2).post(post).build());

        // when
        List<Like> likes = likeRepository.findAll();

        // then
        assertThat(likes).hasSize(2);
        assertThat(likes).extracting("post").containsOnly(post);
        assertThat(likes).extracting("user").containsExactlyInAnyOrder(user1, user2);
    }
}
