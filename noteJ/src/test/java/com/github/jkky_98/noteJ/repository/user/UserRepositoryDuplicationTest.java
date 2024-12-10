package com.github.jkky_98.noteJ.repository.user;

import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.domain.user.UserRole;
import com.github.jkky_98.noteJ.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
public class UserRepositoryDuplicationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("[UserRepository] 이메일 중복 시나리오 테스트 (DB레벨)")
    public void testDuplicateEmailNotAllowed() {
        // Given
        User user1 = User.builder()
                .username("user1")
                .email("duplicate@example.com")
                .password("password123")
                .userRole(UserRole.USER)
                .build();

        User user2 = User.builder()
                .username("user2")
                .email("duplicate@example.com") // 같은 이메일
                .password("password123")
                .userRole(UserRole.USER)
                .build();

        // When
        userRepository.saveAndFlush(user1); // 테스트 트랜잭션에선 무조건 롤백되기 때문에 플러시 로직 담긴 save메서드 사용

        // Then
        assertThatThrownBy(() -> userRepository.saveAndFlush(user2)) // 중복 저장 시 예외 발생
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
