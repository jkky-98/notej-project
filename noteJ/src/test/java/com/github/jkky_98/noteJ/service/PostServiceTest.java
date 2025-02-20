package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.Post;
import com.github.jkky_98.noteJ.domain.PostTag;
import com.github.jkky_98.noteJ.domain.Tag;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.repository.PostRepository;
import com.github.jkky_98.noteJ.repository.TagRepository;
import com.github.jkky_98.noteJ.service.dto.DeletePostToServiceDto;
import com.github.jkky_98.noteJ.web.controller.dto.PostViewDto;
import com.github.jkky_98.noteJ.domain.mapper.PostMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("[PostService] Unit Tests")
class PostServiceTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private TagRepository tagRepository;
    @Mock
    private UserService userService;
    @Mock
    private PostMapper postMapper;

    @InjectMocks
    private PostService postService;

    private User user;       // 세션 유저, post 삭제 시 본인이어야 함
    private Post post;       // 테스트용 게시글
    private User postOwner;  // 게시글 소유자 (삭제 유효성을 위해 user와 일치하거나 다르게 설정)

    private PostViewDto postViewDto;

    @BeforeEach
    void setUp() {
        // 세션 유저 (삭제를 위한 user)
        user = User.builder()
                .id(1L)
                .username("user1")
                .build();

        // 게시글 소유자 (초기에는 동일하게 설정하여 valid delete가 가능하도록 함)
        postOwner = User.builder()
                .id(1L)
                .username("user1")
                .build();

        // 테스트용 Post 객체 (빌더 패턴 가정)
        post = Post.builder()
                .id(10L)
                .title("Test Post")
                .user(postOwner)
                .build();

        // PostViewDto 생성 (dummy)
        postViewDto = PostViewDto.builder().build();
        // 예시로 필요한 필드를 설정 (예: title)
        // postViewDto.setTitle("Test Post");
    }

    @Test
    @DisplayName("findByPostUrl: 존재하는 포스트 반환")
    void testFindByPostUrl_found() {
        // given
        String url = "test-url";
        when(postRepository.findByPostUrl(url)).thenReturn(Optional.of(post));

        // when
        Post found = postService.findByPostUrl(url);

        // then
        assertNotNull(found);
        assertEquals(post.getId(), found.getId());
        verify(postRepository).findByPostUrl(url);
    }

    @Test
    @DisplayName("findByPostUrl: 포스트 미존재 시 예외 발생")
    void testFindByPostUrl_notFound() {
        String url = "not-found";
        when(postRepository.findByPostUrl(url)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> postService.findByPostUrl(url));
        assertEquals("Post Not Found", ex.getMessage());
    }

    @Test
    @DisplayName("findById: 존재하는 포스트 반환")
    void testFindById_found() {
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));

        Post found = postService.findById(10L);
        assertNotNull(found);
        assertEquals(post.getId(), found.getId());
        verify(postRepository).findById(10L);
    }

    @Test
    @DisplayName("findById: 포스트 미존재 시 예외 발생")
    void testFindById_notFound() {
        when(postRepository.findById(10L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> postService.findById(10L));
        assertEquals("Post not found", ex.getMessage());
    }

    @Test
    @DisplayName("getPost: 포스트 DTO 반환")
    void testGetPost() {
        String url = "test-url";
        when(postRepository.findByPostUrl(url)).thenReturn(Optional.of(post));
        when(postMapper.toPostViewDto(post)).thenReturn(postViewDto);

        PostViewDto dto = postService.getPost(url);
        assertNotNull(dto);
        verify(postRepository).findByPostUrl(url);
        verify(postMapper).toPostViewDto(post);
    }

    @Test
    @DisplayName("evictTagCache: 캐시 제거 메서드 실행")
    void testEvictTagCache() {
        // 내부 로직이 없으므로, 단순히 메서드 호출 시 예외가 발생하지 않는지 확인
        Long userId = 1L;
        assertDoesNotThrow(() -> postService.evictTagCache(userId));
    }
    @Test
    @DisplayName("deletePost: 정상 포스트 삭제")
    void testDeletePost_success() {
        String url = "test-url";
        Long sessionUserId = 1L;
        DeletePostToServiceDto dto = new DeletePostToServiceDto(url, sessionUserId);

        // validDeletePostAndGetRemovedPostId: userService.findUserById와 findByPostUrl 호출
        when(userService.findUserById(sessionUserId)).thenReturn(user);
        when(postRepository.findByPostUrl(url)).thenReturn(Optional.of(post));

        // 유효한 삭제를 위해 post의 소유자를 세션 유저와 동일하게 설정
        post = Post.builder()
                .id(10L)
                .title("Test Post")
                .user(user)
                .build();

        // findById(postId) 스텁 추가
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));

        // 포스트에 연결된 태그를 가진 PostTag 리스트 생성
        Tag tag1 = Tag.builder().build();
        Tag tag2 = Tag.builder().build();
        PostTag postTag1 = PostTag.builder().tag(tag1).post(post).build();
        PostTag postTag2 = PostTag.builder().tag(tag2).post(post).build();

        // Post 엔티티에 setPostTags()가 없다면, getPostTags()가 초기화된 리스트를 반환한다고 가정하고, 그 리스트에 태그들을 추가합니다.
        // 만약 getPostTags()가 null이라면, 테스트용으로 강제로 초기화할 수 있습니다.
        List<PostTag> postTags = post.getPostTags();
        if (postTags == null) {
            postTags = new ArrayList<>();
            // Reflection 등을 통해 내부 필드를 주입할 수 있으나, 여기서는 테스트 시 Post 빌더가 초기화된 리스트를 반환한다고 가정합니다.
        } else {
            postTags.clear();
        }
        postTags.add(postTag1);
        postTags.add(postTag2);

        // when
        assertDoesNotThrow(() -> postService.deletePost(dto));

        // then
        verify(tagRepository).deleteAll(anyList());
        verify(postRepository).delete(post);
        // evictTagCache 호출은 @CacheEvict이므로 직접 검증하기 어렵습니다.
    }


    @Test
    @DisplayName("deletePost: 세션유저와 포스트 소유자 불일치 시 예외 발생")
    void testDeletePost_userMismatch() {
        String url = "test-url";
        Long sessionUserId = 1L;
        DeletePostToServiceDto dto = new DeletePostToServiceDto(url, sessionUserId);

        // 세션 유저: user (id = 1)
        when(userService.findUserById(sessionUserId)).thenReturn(user);

        // 포스트 소유자는 별도로 생성 (id = 2)
        User mismatchedOwner = User.builder()
                .id(2L)
                .username("user2")
                .build();
        // 불일치하는 Post 객체 생성
        Post mismatchedPost = Post.builder()
                .id(10L)
                .title("Test Post")
                .user(mismatchedOwner)
                .build();
        when(postRepository.findByPostUrl(url)).thenReturn(Optional.of(mismatchedPost));

        // validDeletePostAndGetRemovedPostId()에서 세션 유저(user.id=1)와
        // 포스트 소유자(mismatchedOwner.id=2)가 불일치하여 IllegalArgumentException이 발생해야 함.
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> postService.deletePost(dto));
        assertEquals("세션유저와 post유저의 유저네임 일치하지 않음", ex.getMessage());
    }
}