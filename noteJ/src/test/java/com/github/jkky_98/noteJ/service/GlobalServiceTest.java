package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.domain.mapper.GlobalMapper;
import com.github.jkky_98.noteJ.web.controller.form.UserNavigationViewForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("[GlobalService] Unit Tests")
class GlobalServiceTest {

    @Mock
    private GlobalMapper globalMapper;

    @Mock
    private UserService userService;

    @InjectMocks
    private GlobalService globalService;

    private User testUser;
    private UserNavigationViewForm expectedViewForm;

    @BeforeEach
    void setUp() {
        // 테스트용 User 객체 생성 (빌더 패턴 가정)
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .build();

        // 테스트용 UserNavigationViewForm 생성 (필요한 필드 설정)
        expectedViewForm = new UserNavigationViewForm();
        expectedViewForm.setUsername("testuser");
        expectedViewForm.setEmail("testuser@example.com");
        expectedViewForm.setProfilePic("profilePic.png");
        expectedViewForm.setUserDesc("Test description");
        expectedViewForm.setBlogTitle("Test Blog Title");
    }

    @Test
    @DisplayName("[GlobalService] getNavigationWithSessionUser: 정상적으로 DTO 반환")
    void testGetNavigationWithSessionUser() {
        Long userId = 1L;

        // stub 설정: userService와 globalMapper가 예상대로 동작
        when(userService.findUserById(userId)).thenReturn(testUser);
        when(globalMapper.toUserNavigationViewForm(testUser)).thenReturn(expectedViewForm);

        // when
        UserNavigationViewForm result = globalService.getNavigationWithSessionUser(userId);

        // then
        assertNotNull(result, "결과 DTO는 null이 아니어야 합니다.");
        assertEquals(expectedViewForm, result, "매핑된 DTO가 예상한 값과 일치해야 합니다.");

        verify(userService).findUserById(userId);
        verify(globalMapper).toUserNavigationViewForm(testUser);
    }
}
