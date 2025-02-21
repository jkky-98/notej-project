package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.repository.UserRepository;
import com.github.jkky_98.noteJ.web.controller.dto.TagCountDto;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("[TagService] Unit Tests")
class TagServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TagService tagService;

    private User user;
    private List<TagCountDto> tagList;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testUser")
                .build();

        tagList = List.of(
                new TagCountDto("Java", 10L),
                new TagCountDto("Spring", 5L),
                new TagCountDto("JPA", 3L)
        );
    }

    @Test
    @DisplayName("getAllTag() - 사용자의 태그 목록 조회")
    void testGetAllTag() {
        // given
        when(userService.findUserById(user.getId())).thenReturn(user);
        when(userRepository.findTagsByUser(user.getId())).thenReturn(tagList);

        // when
        List<TagCountDto> result = tagService.getAllTag(user.getId());

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getTagName()).isEqualTo("Java");
        assertThat(result.get(1).getTagName()).isEqualTo("Spring");
        assertThat(result.get(2).getTagName()).isEqualTo("JPA");

        verify(userService, times(1)).findUserById(user.getId());
        verify(userRepository, times(1)).findTagsByUser(user.getId());
    }

    @Test
    @DisplayName("getAllTag() - 존재하지 않는 사용자 ID 조회 시 예외 발생")
    void testGetAllTag_UserNotFound() {
        // given
        when(userService.findUserById(user.getId())).thenThrow(new EntityNotFoundException("User not found"));

        // when & then
        assertThrows(EntityNotFoundException.class, () -> tagService.getAllTag(user.getId()));

        verify(userService, times(1)).findUserById(user.getId());
        verify(userRepository, never()).findTagsByUser(anyLong()); // 사용자 없으면 태그 조회 안 함
    }
}