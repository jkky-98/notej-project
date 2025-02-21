package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("[UserService] Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testUser")
                .build();
    }

    @Test
    @DisplayName("findUserById() - ID로 사용자 조회 성공")
    void testFindUserById() {
        // given
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        // when
        User result = userService.findUserById(user.getId());

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(user.getId());
        assertThat(result.getUsername()).isEqualTo("testUser");

        verify(userRepository, times(1)).findById(user.getId());
    }

    @Test
    @DisplayName("findUserById() - 존재하지 않는 사용자 ID 조회 시 예외 발생")
    void testFindUserById_UserNotFound() {
        // given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(EntityNotFoundException.class, () -> userService.findUserById(999L));

        verify(userRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("findUserByUsername() - Username으로 사용자 조회 성공")
    void testFindUserByUsername() {
        // given
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

        // when
        User result = userService.findUserByUsername(user.getUsername());

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testUser");

        verify(userRepository, times(1)).findByUsername(user.getUsername());
    }

    @Test
    @DisplayName("findUserByUsername() - 존재하지 않는 Username 조회 시 예외 발생")
    void testFindUserByUsername_UserNotFound() {
        // given
        when(userRepository.findByUsername("unknownUser")).thenReturn(Optional.empty());

        // when & then
        assertThrows(EntityNotFoundException.class, () -> userService.findUserByUsername("unknownUser"));

        verify(userRepository, times(1)).findByUsername("unknownUser");
    }
}
