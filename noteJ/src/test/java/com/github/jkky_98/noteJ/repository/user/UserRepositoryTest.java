package com.github.jkky_98.noteJ.repository.user;

import com.github.jkky_98.noteJ.domain.Post;
import com.github.jkky_98.noteJ.domain.PostTag;
import com.github.jkky_98.noteJ.domain.Tag;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.domain.user.UserDesc;
import com.github.jkky_98.noteJ.domain.user.UserRole;
import com.github.jkky_98.noteJ.repository.PostRepository;
import com.github.jkky_98.noteJ.repository.PostTagRepository;
import com.github.jkky_98.noteJ.repository.TagRepository;
import com.github.jkky_98.noteJ.repository.UserRepository;
import com.github.jkky_98.noteJ.web.controller.dto.TagCountDto;
import jakarta.persistence.EntityManager;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

//@DataJpaTest
@SpringBootTest
@Transactional
public class UserRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PostTagRepository postTagRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    TagRepository tagRepository;

    private Validator validator;

    @BeforeEach
    public void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("[UserRepository] User 엔티티 영속성 컨텍스트 persist 성공 시나리오 테스트")
    void testSaveUser() {
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
    @DisplayName("[UserRepository] User 엔티티 findById 성공 시나리오 테스트")
    void testFindById() {
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
    @DisplayName("[UserRepository] User와 UserDesc 연관관계 매핑 테스트")
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
    @DisplayName("[UserRepository] User와 Post의 연관관계 매핑 테스트 - User 저장 시 연관된 Post도 함께 저장되는지 확인")
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
    @DisplayName("[UserRepository] User 전체 조회 테스트")
    public void testFindUserAll() {
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
        for (User user : users) {
            System.out.println("user = " + user);
        }

        // Then
        assertThat(users).hasSize(2);
    }

    @Test
    @DisplayName("[UserRepository] User 삭제 테스트")
    public void testDeleteUserOne() {
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
    @DisplayName("[UserRepository] User 저장 후 존재 여부 확인 테스트")
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
    @DisplayName("[UserRepository] User 수 조회 테스트")
    public void testCountUser() {
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
    void testFindByUsername() {
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
    void testFindByEmail() {
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
    @DisplayName("[UserRepository] findByUsername: User 없는 경우 테스트")
    void testFindByUsernameNotFound() {
        // when
        Optional<User> result = userRepository.findByUsername("nonexistentuser");

        // then
        assertThat(result).isNotPresent();
    }

    @Test
    @DisplayName("[UserRepository] findByEmail: Email 없는 경우 테스트")
    void testFindByEmailNotFound() {
        // when
        Optional<User> result = userRepository.findByEmail("nonexistent@example.com");

        // then
        assertThat(result).isNotPresent();
    }

    @Test
    @DisplayName("[UserRepository] findTagsByUser - 유저의 태그 및 카운트를 반환")
    void findTagsByUserTest() {
        // given
        User user = User.builder()
                .username("test_user")
                .email("hi@google.com")
                .password("password@@")
                .userRole(UserRole.USER)
                .build();

        Post post1 = Post.builder().title("Post 1").content("Content 1").user(user).build();
        Post post2 = Post.builder().title("Post 2").content("Content 2").user(user).build();

        Tag tag1 = Tag.builder().name("Tag 1").build();
        Tag tag2 = Tag.builder().name("Tag 2").build();

        PostTag postTag1 = PostTag.builder().post(post1).tag(tag1).build();
        PostTag postTag2 = PostTag.builder().post(post1).tag(tag2).build();
        PostTag postTag3 = PostTag.builder().post(post2).tag(tag1).build();

        // 연관 관계 설정 --> 연관관계 비주인에 설정하면 뭐하니 위에서 .user로 설정해야지...
        user.getPosts().add(post1);
        user.getPosts().add(post2);

        post1.getPostTags().add(postTag1);
        post2.getPostTags().add(postTag2);
        post2.getPostTags().add(postTag3);

        // 저장 순서 중요
        tagRepository.saveAll(List.of(tag1, tag2)); // 태그 먼저 저장
        userRepository.save(user); // 유저와 연관된 포스트 저장
        postRepository.saveAll(List.of(post1, post2)); // 포스트 저장
        postTagRepository.saveAll(List.of(postTag1, postTag2, postTag3)); // PostTag 저장

        // when
        List<TagCountDto> results = userRepository.findTagsByUser(1L);
        System.out.println(results);

        // then
        assertThat(results).isNotEmpty();
        assertThat(results).hasSize(2);

        TagCountDto tagCount1 = results.stream().filter(dto -> dto.getTagName().equals("Tag 1")).findFirst().orElse(null);
        TagCountDto tagCount2 = results.stream().filter(dto -> dto.getTagName().equals("Tag 2")).findFirst().orElse(null);

        assertThat(tagCount1).isNotNull();
        assertThat(tagCount1.getCount()).isEqualTo(2);

        assertThat(tagCount2).isNotNull();
        assertThat(tagCount2.getCount()).isEqualTo(1);
    }
}
