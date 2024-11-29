package com.github.jkky_98.noteJ.repository.user;

import com.github.jkky_98.noteJ.domain.Post;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.domain.user.UserRole;
import com.github.jkky_98.noteJ.repository.PostRepository;
import com.github.jkky_98.noteJ.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class UserCascadeTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Test
    @DisplayName("User 저장 시 연관된 Post가 Cascade Persist로 함께 저장되는지 확인")
    public void testCascadePersist() {
        // Given
        User user = User.builder()
                .username("testuser")
                .email("testuser@example.com")
                .password("password123")
                .userRole(UserRole.USER)
                .build();

        Post post1 = Post.builder()
                .title("Post 1")
                .content("Content of Post 1")
                .user(user)
                .is_private(false)
                .build();

        Post post2 = Post.builder()
                .title("Post 2")
                .content("Content of Post 2")
                .user(user)
                .is_private(true)
                .build();

        user.getPosts().add(post1);
        user.getPosts().add(post2);

        // When
        userRepository.save(user); // Cascade로 Post도 저장됨

        // Then
        List<Post> posts = postRepository.findAll();
        assertThat(posts).hasSize(2);
        assertThat(posts.get(0).getUser()).isEqualTo(user);
        assertThat(posts.get(1).getUser()).isEqualTo(user);
    }

    @Test
    @DisplayName("User에서 Post를 제거하면 orphanRemoval로 Post가 삭제되는지 확인")
    public void testOrphanRemoval() {
        // Given
        User user = User.builder()
                .username("testuser")
                .email("testuser@example.com")
                .password("password123")
                .userRole(UserRole.USER)
                .build();

        Post post1 = Post.builder()
                .title("Post 1")
                .content("Content of Post 1")
                .user(user)
                .is_private(false)
                .build();

        user.getPosts().add(post1);
        userRepository.save(user); // 저장

        // When
        user.getPosts().remove(post1); // 연관관계에서 제거
        userRepository.saveAndFlush(user); // 강제로 플러시하여 데이터베이스 반영

        // Then
        List<Post> posts = postRepository.findAll();
        assertThat(posts).isEmpty(); // Post도 삭제됨
    }

    @Test
    @DisplayName("User 삭제 시 연관된 Post가 Cascade Remove로 함께 삭제되는지 확인")
    public void testCascadeDelete() {
        // Given
        User user = User.builder()
                .username("testuser")
                .email("testuser@example.com")
                .password("password123")
                .userRole(UserRole.USER)
                .build();

        Post post1 = Post.builder()
                .title("Post 1")
                .content("Content of Post 1")
                .user(user)
                .is_private(false)
                .build();

        Post post2 = Post.builder()
                .title("Post 2")
                .content("Content of Post 2")
                .user(user)
                .is_private(true)
                .build();

        user.getPosts().add(post1);
        user.getPosts().add(post2);

        userRepository.save(user);

        // When
        userRepository.delete(user); // Cascade로 Post도 삭제됨

        // Then
        List<Post> posts = postRepository.findAll();
        assertThat(posts).isEmpty(); // Post 삭제 확인
    }
}
