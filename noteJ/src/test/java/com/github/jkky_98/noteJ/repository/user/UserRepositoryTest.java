package com.github.jkky_98.noteJ.repository.user;

import com.github.jkky_98.noteJ.domain.Post;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.domain.user.UserDesc;
import com.github.jkky_98.noteJ.domain.user.UserRole;
import com.github.jkky_98.noteJ.repository.UserRepository;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    private Validator validator;

    @BeforeEach
    public void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("유저 엔티티 영속성 컨텍스트 persist 성공 시나리오 테스트")
    void saveUserTest() {
    	// given
        User testUser = User.builder()
                .username("tester1")
                .email("aal2525@ajou.ac.kr")
                .password("testpw")
                .userRole(UserRole.USER)
                .build();
        // when
        User saveUser = userRepository.save(testUser);

        // then
        assertThat(saveUser).isEqualTo(testUser);
    }

    @Test
    @DisplayName("유저 엔티티 findById 성공 시나리오 테스트")
    void findByIdTest() {
    	// given
        User testUser = User.builder()
                .username("tester1")
                .email("aal2525@ajou.ac.kr")
                .password("testpw")
                .userRole(UserRole.USER)
                .build();
        User saveUser = userRepository.save(testUser);
        // when
        User findUser = userRepository.findById(saveUser.getId()).get();

        // then
        assertThat(findUser).isEqualTo(saveUser);
    }

    @Test
    @DisplayName("유저 엔티티 이메일 형식 실패 시나리오 테스트")
    public void testUserAndUserDescRelationship() {
        // Given
        UserDesc userDesc = UserDesc.builder()
                .description("This is a test user description.")
                .build();

        User user = User.builder()
                .username("testuser")
                .email("testuser@example.com")
                .password("password123")
                .userRole(UserRole.USER)
                .userDesc(userDesc) // 연관관계 설정
                .build();

        // When
        userRepository.save(user);

        // Then
        User foundUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(foundUser.getUserDesc()).isNotNull();
        assertThat(foundUser.getUserDesc().getDescription()).isEqualTo("This is a test user description.");
    }

    @Test
    @DisplayName("User와 Post의 연관관계 매핑 테스트 - User 저장 시 연관된 Post도 함께 저장되는지 확인")
    public void testUserAndPostsRelationship() {
        // Given
        User user = User.builder()
                .username("postuser")
                .email("postuser@example.com")
                .password("password123")
                .userRole(UserRole.USER)
                .build();

        Post post1 = Post.builder()
                .title("First Post")
                .content("Content of the first post")
                .user(user)
                .build();

        Post post2 = Post.builder()
                .title("Second Post")
                .content("Content of the second post")
                .user(user)
                .build();

        user.getPosts().add(post1);
        user.getPosts().add(post2);

        // When
        userRepository.save(user);

        // Then
        User foundUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(foundUser.getPosts()).hasSize(2);
        assertThat(foundUser.getPosts().get(0).getTitle()).isEqualTo("First Post");
        assertThat(foundUser.getPosts().get(1).getTitle()).isEqualTo("Second Post");
    }

    @Test
    @DisplayName("사용자 전체 조회 테스트")
    public void testFindAll() {
        // Given
        User user1 = User.builder()
                .username("user1")
                .email("user1@example.com")
                .password("password123")
                .userRole(UserRole.USER)
                .build();

        User user2 = User.builder()
                .username("user2")
                .email("user2@example.com")
                .password("password123")
                .userRole(UserRole.USER)
                .build();

        userRepository.save(user1);
        userRepository.save(user2);

        // When
        Iterable<User> users = userRepository.findAll(); // 전체 조회

        // Then
        assertThat(users).hasSize(2);
    }

    @Test
    @DisplayName("사용자 삭제 테스트")
    public void testDelete() {
        // Given
        User user = User.builder()
                .username("testuser")
                .email("testuser@example.com")
                .password("password123")
                .userRole(UserRole.USER)
                .build();

        User savedUser = userRepository.save(user);

        // When
        userRepository.delete(savedUser); // 삭제
        Optional<User> deletedUser = userRepository.findById(savedUser.getId()); // 삭제 확인

        // Then
        assertThat(deletedUser).isNotPresent();
    }

    @Test
    @DisplayName("사용자 존재 여부 확인 테스트")
    public void testExistsById() {
        // Given
        User user = User.builder()
                .username("testuser")
                .email("testuser@example.com")
                .password("password123")
                .userRole(UserRole.USER)
                .build();

        User savedUser = userRepository.save(user);

        // When
        boolean exists = userRepository.existsById(savedUser.getId()); // 존재 여부 확인

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("사용자 수 조회 테스트")
    public void testCount() {
        // Given
        User user1 = User.builder()
                .username("user1")
                .email("user1@example.com")
                .password("password123")
                .userRole(UserRole.USER)
                .build();

        User user2 = User.builder()
                .username("user2")
                .email("user2@example.com")
                .password("password123")
                .userRole(UserRole.USER)
                .build();

        userRepository.save(user1);
        userRepository.save(user2);

        // When
        long userCount = userRepository.count(); // 사용자 수 조회

        // Then
        assertThat(userCount).isEqualTo(2);
    }

    @Test
    @DisplayName("[UserRepository] findByUsername 메서드 테스트")
    void findByUsernameTest() {
        // given
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .userRole(UserRole.USER)
                .build();
        userRepository.save(user);

        // when
        Optional<User> result = userRepository.findByUsername("testuser");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("testuser");
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("[UserRepository] findByEmail 메서드 테스트")
    void findByEmailTest() {
        // given
        User user = User.builder()
                .username("anotheruser")
                .email("another@example.com")
                .password("password456")
                .userRole(UserRole.USER)
                .build();
        userRepository.save(user);

        // when
        Optional<User> result = userRepository.findByEmail("another@example.com");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("anotheruser");
        assertThat(result.get().getEmail()).isEqualTo("another@example.com");
    }

    @Test
    @DisplayName("[UserRepository] findByUsername 없는 경우 테스트")
    void findByUsernameNotFoundTest() {
        // when
        Optional<User> result = userRepository.findByUsername("nonexistentuser");

        // then
        assertThat(result).isNotPresent();
    }

    @Test
    @DisplayName("[UserRepository] findByEmail 없는 경우 테스트")
    void findByEmailNotFoundTest() {
        // when
        Optional<User> result = userRepository.findByEmail("nonexistent@example.com");

        // then
        assertThat(result).isNotPresent();
    }
}
