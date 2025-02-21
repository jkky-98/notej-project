package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.mapper.ProfileMapper;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.web.controller.form.ProfileForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("[ProfileService] Unit Tests")
public class ProfileServiceTest {

    @Mock
    private UserService userService; // Mock UserService

    @Mock
    private ProfileMapper profileMapper; // Mock ProfileMapper

    @InjectMocks
    private ProfileService profileService; // 실제 테스트 대상

    private User user;
    private ProfileForm profileForm;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testUser")
                .email("test@example.com")
                .build();

        profileForm = ProfileForm.builder()
                        .username("testUser")
                                .socialEmail("test@example.com").build();

    }

    @Test
    @DisplayName("getProfile() - 정상적인 프로필 조회")
    void testGetProfile() {
        // Given
        Long userId = 1L;
        when(userService.findUserById(userId)).thenReturn(user);
        when(profileMapper.toProfileForm(user)).thenReturn(profileForm);

        // When
        ProfileForm result = profileService.getProfile(userId);

        // Then

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testUser");
        assertThat(result.getSocialEmail()).isEqualTo("test@example.com");

        // 특정 메서드가 올바르게 호출되었는지 검증
        verify(userService, times(1)).findUserById(userId);
        verify(profileMapper, times(1)).toProfileForm(user);
    }

    @Test
    @DisplayName("getProfile() - 존재하지 않는 사용자 ID 조회 시 예외 발생")
    void testGetProfileUserNotFound() {
        // Given
        Long userId = 999L;
        when(userService.findUserById(userId)).thenThrow(new IllegalArgumentException("User not found"));

        // When & Then
        IllegalArgumentException exception =
                org.junit.jupiter.api.Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> profileService.getProfile(userId)
                );

        assertThat(exception.getMessage()).isEqualTo("User not found");

        // userService.findUserById()가 호출되었는지 검증
        verify(userService, times(1)).findUserById(userId);
        verify(profileMapper, never()).toProfileForm(any(User.class)); // 예외가 발생했으므로 호출되지 않아야 함
    }
}
