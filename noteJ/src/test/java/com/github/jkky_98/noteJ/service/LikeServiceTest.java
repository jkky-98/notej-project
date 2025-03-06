package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.Like;
import com.github.jkky_98.noteJ.domain.Post;
import com.github.jkky_98.noteJ.domain.mapper.LikeMapper;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.exception.LikeBadRequestClientException;
import com.github.jkky_98.noteJ.exception.LikeSelfSaveClientException;
import com.github.jkky_98.noteJ.repository.LikeRepository;
import com.github.jkky_98.noteJ.service.dto.DeleteLikeToServiceDto;
import com.github.jkky_98.noteJ.service.dto.SaveLikeToServiceDto;
import com.github.jkky_98.noteJ.web.controller.dto.LikeListByPostDto;
import com.github.jkky_98.noteJ.web.controller.dto.LikeStatusForm;
import com.github.jkky_98.noteJ.web.controller.form.LikeCardForm;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("[LikeService] Unit Tests")
class LikeServiceTest {

    @Mock
    private UserService userService;
    @Mock
    private LikeRepository likeRepository;
    @Mock
    private PostService postService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private LikeMapper likeMapper;

    @InjectMocks
    private LikeService likeService;

    private User user;   // 좋아요를 누르는 사용자
    private Post post;   // 테스트용 게시글
    private User postOwner; // 게시글 소유자 (user와 다름)

    @BeforeEach
    void setUp() {
        // 테스트용 User 객체 (빌더 패턴 사용 가정)
        user = User.builder()
                .id(1L)
                .username("user1")
                .password("123456")
                .email("user1@gmail.com")
                .likes(new ArrayList<>())
                .followingList(new ArrayList<>())
                .followerList(new ArrayList<>())
                .build();

        // 게시글 소유자: user2 (user와 다른 사용자)
        postOwner = User.builder()
                .id(2L)
                .username("user2")
                .password("123456")
                .email("postOwner@gmail.com")
                .build();

        // 테스트용 Post 객체 (빌더 패턴 사용 가정)
        post = Post.builder()
                .id(10L)
                .title("Test Post")
                .user(postOwner)
                .build();
    }

    @Test
    @DisplayName("getLikeStatus() - 좋아요 상태가 true인 경우")
    void testGetLikeStatus_true() {
        // given
        when(userService.findUserById(1L)).thenReturn(user);
        when(postService.findByPostUrl("test-post")).thenReturn(post);
        when(likeRepository.existsByUserAndPost(user, post)).thenReturn(true);

        // when
        LikeStatusForm result = likeService.getLikeStatus("test-post", 1L);

        // then
        assertTrue(result.isLiked(), "좋아요 상태가 true여야 합니다.");
        verify(userService).findUserById(1L);
        verify(postService).findByPostUrl("test-post");
        verify(likeRepository).existsByUserAndPost(user, post);
    }

    @Test
    @DisplayName("getLikeStatus() - 좋아요 상태가 false인 경우")
    void testGetLikeStatus_false() {
        // given
        when(userService.findUserById(1L)).thenReturn(user);
        when(postService.findByPostUrl("test-post")).thenReturn(post);
        when(likeRepository.existsByUserAndPost(user, post)).thenReturn(false);

        // when
        LikeStatusForm result = likeService.getLikeStatus("test-post", 1L);

        // then
        assertFalse(result.isLiked(), "좋아요 상태가 false여야 합니다.");
        verify(userService).findUserById(1L);
        verify(postService).findByPostUrl("test-post");
        verify(likeRepository).existsByUserAndPost(user, post);
    }

    @Test
    @DisplayName("saveLike() - 이미 좋아요 상태이면 예외 발생")
    void testSaveLike_whenAlreadyLiked_shouldThrowException() {
        // given
        SaveLikeToServiceDto dto = new SaveLikeToServiceDto();
        dto.setLiked(true); // 이미 좋아요 상태

        // when & then
        LikeBadRequestClientException ex = assertThrows(LikeBadRequestClientException.class,
                () -> likeService.saveLike(dto));
        assertEquals("좋아요 상태가 역전되어 있습니다.", ex.getMessage());
    }

    @Test
    @DisplayName("saveLike() - 자기 자신의 게시글에 좋아요 시도 시 예외 발생")
    void testSaveLike_whenSelfLike_shouldThrowException() {
        // given
        SaveLikeToServiceDto dto = new SaveLikeToServiceDto();
        dto.setLiked(false);
        dto.setPostUrl("test-post");
        dto.setUserId(1L);

        // 자기 자신의 게시글로 설정
        Post postTest = Post.builder()
                .id(10L)
                .title("Test Post")
                .user(user)
                .build();

        when(postService.findByPostUrl("test-post")).thenReturn(postTest);
        when(userService.findUserById(1L)).thenReturn(user);

        // when & then
        LikeSelfSaveClientException ex = assertThrows(LikeSelfSaveClientException.class,
                () -> likeService.saveLike(dto));
        assertEquals("스스로에게 좋아요는 불가능 합니다.", ex.getMessage());
    }

    @Test
    @DisplayName("saveLike() - 정상 좋아요 저장")
    void testSaveLike_success() {
        // given
        SaveLikeToServiceDto dto = new SaveLikeToServiceDto();
        dto.setLiked(false);
        dto.setPostUrl("test-post");
        dto.setUserId(1L);

        // 게시글 소유자는 postOwner (user와 다름)
        when(postService.findByPostUrl("test-post")).thenReturn(post);
        when(userService.findUserById(1L)).thenReturn(user);

        Like like = Like.builder()
                .user(user)
                .userGetLike(post.getUser())
                .post(post)
                .build();

        when(likeMapper.toLike(user, post)).thenReturn(like);
        when(likeRepository.save(any(Like.class))).thenReturn(like);

        // when
        assertDoesNotThrow(() -> likeService.saveLike(dto));

        // then
        verify(likeMapper).toLike(user, post);
        verify(likeRepository).save(like);
        verify(notificationService).sendLikePostNotification(eq(post.getUser()), eq(user), eq(post.getTitle()));
    }

    @Test
    @DisplayName("deleteLike() - 좋아요 상태가 false이면 예외 발생")
    void testDeleteLike_whenNotLiked_shouldThrowException() {
        // given
        DeleteLikeToServiceDto dto = new DeleteLikeToServiceDto();
        dto.setLiked(false);

        // when & then
        LikeBadRequestClientException ex = assertThrows(LikeBadRequestClientException.class,
                () -> likeService.deleteLike(dto));
        assertEquals("좋아요 상태가 역전되어 있습니다.", ex.getMessage());
    }

    @Test
    @DisplayName("deleteLike() - 정상 좋아요 삭제")
    void testDeleteLike_success() {
        // given
        DeleteLikeToServiceDto dto = new DeleteLikeToServiceDto();
        dto.setLiked(true);
        dto.setPostUrl("test-post");
        dto.setUserId(1L);

        when(postService.findByPostUrl("test-post")).thenReturn(post);
        when(userService.findUserById(1L)).thenReturn(user);
        Like like = Like.builder()
                .user(user)
                .userGetLike(post.getUser())
                .post(post)
                .build();
        when(likeRepository.findByUserAndPost(user, post)).thenReturn(Optional.of(like));

        // when
        assertDoesNotThrow(() -> likeService.deleteLike(dto));

        // then
        verify(likeRepository).delete(like);
    }

    @Test
    @DisplayName("deleteLike() - 좋아요가 존재하지 않으면 예외 발생")
    void testDeleteLike_whenLikeNotFound_shouldThrowException() {
        // given
        DeleteLikeToServiceDto dto = new DeleteLikeToServiceDto();
        dto.setLiked(true);
        dto.setPostUrl("test-post");
        dto.setUserId(1L);

        when(postService.findByPostUrl("test-post")).thenReturn(post);
        when(userService.findUserById(1L)).thenReturn(user);
        when(likeRepository.findByUserAndPost(user, post)).thenReturn(Optional.empty());

        // when & then
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> likeService.deleteLike(dto));
        assertEquals("like not found", ex.getMessage());
    }

    @Test
    @DisplayName("getLikeListByPostUrl() - 게시글의 좋아요 목록 반환")
    void testGetLikeListByPostUrl() {
        // given
        when(postService.findByPostUrl("test-post")).thenReturn(post);
        // 게시글에 좋아요 1개 추가
        Like like = Like.builder()
                .user(user)
                .userGetLike(post.getUser())
                .post(post)
                .build();
        post.getLikes().add(like);
        // likeMapper를 통해 DTO 매핑
        LikeListByPostDto dto = LikeListByPostDto.builder().build();
        when(likeMapper.toLikeListByPostDto(user)).thenReturn(dto);

        // when
        List<LikeListByPostDto> result = likeService.getLikeListByPostUrl("test-post");

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(postService).findByPostUrl("test-post");
        verify(likeMapper).toLikeListByPostDto(user);
    }

    @Test
    @DisplayName("getLikeCards() - 사용자의 좋아요 카드 목록 반환")
    void testGetLikeCards() {
        // given
        when(userService.findUserById(1L)).thenReturn(user);
        // 사용자가 좋아요한 게시글 추가
        Like like = Like.builder()
                        .post(post)
                                .user(user)
                                        .userGetLike(postOwner)
                                                .build();
        user.getLikes().add(like);
        // likeMapper를 통해 LikeCardForm 매핑
        LikeCardForm cardForm = LikeCardForm.builder().build();
        when(likeMapper.toLikeCardFormByPost(post)).thenReturn(cardForm);

        // when
        List<LikeCardForm> result = likeService.getLikeCards(1L);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userService).findUserById(1L);
        verify(likeMapper).toLikeCardFormByPost(post);
    }
}
