package com.github.jkky_98.noteJ.repository.post;

import com.github.jkky_98.noteJ.domain.Post;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.domain.user.UserRole;
import com.github.jkky_98.noteJ.repository.PostRepository;
import com.github.jkky_98.noteJ.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class PostRepositoryTest {
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Post 저장 및 조회 테스트")
    public void testSaveAndFindById() {
        // Given
        User user = createUser();
        Post post = Post.builder()
                .title("Test Post")
                .content("This is a test post.")
                .thumbnail("https://example.com/thumbnail.jpg")
                .is_private(false)
                .user(user)
                .build();

        // When
        Post savedPost = postRepository.save(post);
        Optional<Post> foundPost = postRepository.findById(savedPost.getId());

        // Then
        assertThat(foundPost).isPresent();
        assertThat(foundPost.get().getTitle()).isEqualTo("Test Post");
        assertThat(foundPost.get().getContent()).isEqualTo("This is a test post.");
    }

    @Test
    @DisplayName("Post 전체 조회 테스트")
    public void testFindAll() {
        // Given
        User user = createUser();
        Post post1 = Post.builder()
                .title("Post 1")
                .content("Content of Post 1")
                .thumbnail("https://example.com/thumbnail1.jpg")
                .is_private(false)
                .user(user)
                .build();

        Post post2 = Post.builder()
                .title("Post 2")
                .content("Content of Post 2")
                .thumbnail("https://example.com/thumbnail2.jpg")
                .is_private(true)
                .user(user)
                .build();

        postRepository.save(post1);
        postRepository.save(post2);

        // When
        Iterable<Post> posts = postRepository.findAll();

        // Then
        assertThat(posts).hasSize(2);
    }

    @Test
    @DisplayName("Post 업데이트 테스트")
    public void testUpdate() {
        // Given
        User user = createUser();
        Post post = Post.builder()
                .title("Old Title")
                .content("Old Content")
                .thumbnail("https://example.com/thumbnail.jpg")
                .is_private(false)
                .user(user)
                .build();

        Post savedPost = postRepository.save(post);

        // When
        savedPost.setTitle("Updated Title");
        savedPost.setContent("Updated Content");
        Post updatedPost = postRepository.save(savedPost);

        // Then
        assertThat(updatedPost.getTitle()).isEqualTo("Updated Title");
        assertThat(updatedPost.getContent()).isEqualTo("Updated Content");
    }

    @Test
    @DisplayName("Post 삭제 테스트")
    public void testDelete() {
        // Given
        User user = createUser();
        Post post = Post.builder()
                .title("Test Post")
                .content("This is a test post.")
                .thumbnail("https://example.com/thumbnail.jpg")
                .is_private(false)
                .user(user)
                .build();

        Post savedPost = postRepository.save(post);

        // When
        postRepository.delete(savedPost);
        Optional<Post> deletedPost = postRepository.findById(savedPost.getId());

        // Then
        assertThat(deletedPost).isNotPresent();
    }

    @Test
    @DisplayName("Post 존재 여부 확인 테스트")
    public void testExistsById() {
        // Given
        User user = createUser();
        Post post = Post.builder()
                .title("Test Post")
                .content("This is a test post.")
                .thumbnail("https://example.com/thumbnail.jpg")
                .is_private(false)
                .user(user)
                .build();

        Post savedPost = postRepository.save(post);

        // When
        boolean exists = postRepository.existsById(savedPost.getId());

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Post 수 조회 테스트")
    public void testCount() {
        // Given
        User user = createUser();
        Post post1 = Post.builder()
                .title("Post 1")
                .content("Content of Post 1")
                .thumbnail("https://example.com/thumbnail1.jpg")
                .is_private(false)
                .user(user)
                .build();

        Post post2 = Post.builder()
                .title("Post 2")
                .content("Content of Post 2")
                .thumbnail("https://example.com/thumbnail2.jpg")
                .is_private(true)
                .user(user)
                .build();

        postRepository.save(post1);
        postRepository.save(post2);

        // When
        long postCount = postRepository.count();

        // Then
        assertThat(postCount).isEqualTo(2);
    }

    private User createUser() {
        User user = User.builder()
                .username("testuser")
                .email("testuser@example.com")
                .password("password123")
                .userRole(UserRole.USER)
                .build();
        return userRepository.save(user);
    }
}
