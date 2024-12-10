package com.github.jkky_98.noteJ.repository.follow;

import com.github.jkky_98.noteJ.domain.Follow;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.repository.FollowRepository;
import com.github.jkky_98.noteJ.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class FollowRepositoryTest {
    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("[FollowRepository] 팔로우 저장 테스트")
    void saveFollowTest() {
        // given
        User follower = userRepository.save(User.builder().username("FollowerUser").build());
        User following = userRepository.save(User.builder().username("FollowingUser").build());

        Follow follow = Follow.builder()
                .follower(follower)
                .following(following)
                .build();

        // when
        Follow savedFollow = followRepository.save(follow);

        // then
        assertThat(savedFollow.getId()).isNotNull();
        assertThat(savedFollow.getFollower()).isEqualTo(follower);
        assertThat(savedFollow.getFollowing()).isEqualTo(following);
    }

    @Test
    @DisplayName("[FollowRepository] 팔로우 조회 테스트")
    void findFollowByIdTest() {
        // given
        User follower = userRepository.save(User.builder().username("FollowerUser").build());
        User following = userRepository.save(User.builder().username("FollowingUser").build());

        Follow follow = followRepository.save(Follow.builder()
                .follower(follower)
                .following(following)
                .build());

        // when
        Optional<Follow> foundFollow = followRepository.findById(follow.getId());

        // then
        assertThat(foundFollow).isPresent();
        assertThat(foundFollow.get().getFollower()).isEqualTo(follower);
        assertThat(foundFollow.get().getFollowing()).isEqualTo(following);
    }

    @Test
    @DisplayName("[FollowRepository] 팔로우 삭제 테스트")
    void deleteFollowTest() {
        // given
        User follower = userRepository.save(User.builder().username("FollowerUser").build());
        User following = userRepository.save(User.builder().username("FollowingUser").build());

        Follow follow = followRepository.save(Follow.builder()
                .follower(follower)
                .following(following)
                .build());

        // when
        followRepository.delete(follow);

        // then
        Optional<Follow> deletedFollow = followRepository.findById(follow.getId());
        assertThat(deletedFollow).isEmpty();
    }

    @Test
    @DisplayName("[FollowRepository] 특정 사용자(follower) 기준으로 팔로우 조회 테스트")
    void findByFollowerTest() {
        // given
        User follower = userRepository.save(User.builder().username("FollowerUser").build());
        User following1 = userRepository.save(User.builder().username("FollowingUser1").build());
        User following2 = userRepository.save(User.builder().username("FollowingUser2").build());

        followRepository.save(Follow.builder().follower(follower).following(following1).build());
        followRepository.save(Follow.builder().follower(follower).following(following2).build());

        // when
        List<Follow> follows = followRepository.findAll(); // FollowRepository에 `findAll` 기본 메서드 사용

        // then
        assertThat(follows).hasSize(2);
        assertThat(follows).extracting("follower").containsOnly(follower);
        assertThat(follows).extracting("following")
                .containsExactlyInAnyOrder(following1, following2);
    }
}