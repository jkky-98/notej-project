package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.Post;
import com.github.jkky_98.noteJ.domain.mapper.PageMapper;
import com.github.jkky_98.noteJ.domain.mapper.PostMapper;
import com.github.jkky_98.noteJ.domain.mapper.SeriesMapper;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.domain.user.UserDesc;
import com.github.jkky_98.noteJ.repository.PostRepository;
import com.github.jkky_98.noteJ.web.controller.dto.*;
import com.github.jkky_98.noteJ.web.controller.form.PostsConditionForm;
import com.github.jkky_98.noteJ.web.controller.form.ProfileForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("[PostsService] Unit Tests")
public class PostsServiceTest {
    @Mock
    private PostRepository postRepository;
    @Mock
    private UserService userService;
    @Mock
    private ProfileService profileService;
    @Mock
    private TagService tagService;
    @Mock
    private FollowService followService;
    @Mock
    private PostMapper postMapper;
    @Mock
    private PageMapper pageMapper;
    @Mock
    private SeriesMapper seriesMapper;

    @InjectMocks
    private PostsService postsService;

    private User user;
    private Post post;

    @BeforeEach
    void setUp() {

        user = User.builder()
                .id(1L)
                .username("testUser")
                .email("testUser@email.com")
                .password("test123")
                .userDesc(
                        UserDesc.builder()
                                .description("testDesc")
                                .blogTitle("testTitle")
                                .build()
                )
                .build();
    }

    @Test
    @DisplayName("getPosts() -  유저 ID로 게시글 목록 일반조회")
    void testGetPosts() {
        //given
        PostsConditionForm form = new PostsConditionForm();
        List<Post> posts = List.of(
                Post.builder()
                        .title("testTitle")
                        .build()
        );
        PostDto postDto = PostDto.builder()
                        .title("testTitle")
                        .build();
        List<PostDto> expectedPostDtos = List.of(postDto);

        when(userService.findUserById(1L)).thenReturn(user);
        when(postRepository.searchPosts(form, user.getId())).thenReturn(posts);
        when(postMapper.toPostDtoList(posts)).thenReturn(expectedPostDtos);
        //when
        List<PostDto> result = postsService.getPosts(1L, form);

        //then
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(expectedPostDtos.size());
        // searchPosts 메서드 실행되었는지 검증
        verify(postRepository).searchPosts(form, user.getId());
    }

    @Test
    @DisplayName("getPostsWithPageable() - 페이지네이션을 적용한 게시글 조회")
    void testGetPostsWithPageable() {
        //given
        String username = "testUser";
        PostsConditionForm form = new PostsConditionForm();
        Pageable pageable = mock(Pageable.class);
        Page<Post> postPage = mock(Page.class);
        List<PostDto> expectedDtos = List.of(PostDto.builder()
                .title("testTitle")
                .build());

        when(userService.findUserByUsername(username)).thenReturn(user);
        when(postRepository.searchPostsWithPage(form, user.getId(), pageable)).thenReturn(postPage);
        when(postMapper.toPostDtoListFromPage(postPage)).thenReturn(expectedDtos);

        //when
        List<PostDto> result = postsService.getPostsWithPageable(username, form, pageable);

        //then
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(expectedDtos.size());
        // searchPosts 메서드 실행되었는지 검증
        verify(postRepository).searchPostsWithPage(form, user.getId(), pageable);
    }

    @Test
    @DisplayName("getPostsInitialPage() - 초기 페이지 데이터 반환")
    void testGetPostsInitialPage() {
        // Given
        GetPostsToServiceDto dto = GetPostsToServiceDto.of(
                "testUser",
                new PostsConditionForm(),
                Optional.ofNullable(user)
                );
        ProfileForm profileForm = ProfileForm.builder().build();
        List<TagCountDto> tags = List.of(new TagCountDto());
        boolean canFollow = true;
        List<PostDto> postDtos = List.of(PostDto.builder().build());
        PostsForm expectedForm = PostsForm.builder().build();

        when(userService.findUserByUsername(dto.getUsernamePost())).thenReturn(user);
        when(profileService.getProfile(user.getId())).thenReturn(profileForm);
        when(tagService.getAllTag(user.getId())).thenReturn(tags);
        when(followService.isFollowing(dto.getUser(), user)).thenReturn(canFollow);
        when(userService.findUserById(1L)).thenReturn(user);
        when(postsService.getPosts(user.getId(), dto.getCondition())).thenReturn(postDtos);
        when(pageMapper.toPostsForm(profileForm, tags, canFollow, postDtos, user.getUsername()))
                .thenReturn(expectedForm);

        // When
        PostsForm result = postsService.getPostsInitialPage(dto);

        // Then
        assertThat(result).isNotNull();
        verify(userService).findUserByUsername(dto.getUsernamePost());
        verify(profileService).getProfile(user.getId());
        verify(tagService).getAllTag(user.getId());
    }

    @Test
    @DisplayName("getSeriesTabs() - 시리즈 목록 반환")
    void testGetSeriesTabs() {
        // Given
        String username = "testUser";
        Optional<User> sessionUser = Optional.of(user);
        ProfileForm profileForm = ProfileForm.builder().build();
        List<TagCountDto> tags = List.of(new TagCountDto());
        List<SeriesViewDto> seriesViewDtos = List.of(SeriesViewDto.builder().build());
        boolean canFollow = true;
        PostsForm expectedForm = PostsForm.builder().build();

        when(userService.findUserByUsername(username)).thenReturn(user);
        when(profileService.getProfile(user.getId())).thenReturn(profileForm);
        when(tagService.getAllTag(user.getId())).thenReturn(tags);
        when(followService.isFollowing(sessionUser, user)).thenReturn(canFollow);
        when(seriesMapper.toSeriesViewDtoList(anyList())).thenReturn(seriesViewDtos);
        when(pageMapper.toPostsFormSeriesPage(profileForm, tags, canFollow, seriesViewDtos, username))
                .thenReturn(expectedForm);

        // When
        PostsForm result = postsService.getSeriesTabs(username, sessionUser);

        // Then
        assertThat(result).isNotNull();
        verify(userService).findUserByUsername(username);
        verify(profileService).getProfile(user.getId());
        verify(tagService).getAllTag(user.getId());
    }

    @Test
    @DisplayName("getPostsNotOpen() - 비공개 게시글 목록 조회")
    void testGetPostsNotOpen() {
        // given
        List<Post> postList = List.of(Post.builder().build());
        List<PostNotOpenDto> expectedDtos = List.of(PostNotOpenDto.builder().build());

        when(userService.findUserById(1L)).thenReturn(user);
        when(postRepository.findAllByUserIdAndWritableFalse(user.getId())).thenReturn(postList);
        when(postMapper.toPostNotOpenDtoList(postList)).thenReturn(expectedDtos);

        // when
        List<PostNotOpenDto> result = postsService.getPostsNotOpen(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(expectedDtos.size());
        verify(postRepository).findAllByUserIdAndWritableFalse(user.getId());
    }

    @Test
    @DisplayName("getPostsGlobalWithPaging() - 전체 게시글 페이징 조회")
    void testGetPostsGlobalWithPaging() {
        // given
        SearchGlobalCondition cond = new SearchGlobalCondition();
        cond.setKeyword("Spring");
        Pageable pageable = Pageable.ofSize(10);

        // 더미 데이터 생성
        List<Post> postList = new ArrayList<>();
        Post post = Post.builder().build();
        // 필요에 따라 post 객체에 필드값 설정
        postList.add(post);

        Page<Post> postsPage = new PageImpl<>(postList, pageable, postList.size());

        // repository와 mapper 동작 정의
        when(postRepository.findAllByWritableTrue(eq(cond.getKeyword()), eq(pageable)))
                .thenReturn(postsPage);

        List<PostDto> expectedDtos = new ArrayList<>();
        PostDto postDto = PostDto.builder().build();
        // 필요에 따라 postDto 객체에 필드값 설정
        expectedDtos.add(postDto);

        when(postMapper.toPostDtoListFromPage(postsPage))
                .thenReturn(expectedDtos);

        // when
        List<PostDto> actualDtos = postsService.getPostsGlobalWithPaging(cond, pageable);

        // then
        assertThat(actualDtos).isNotNull();
        assertThat(actualDtos.size()).isEqualTo(expectedDtos.size());
        assertThat(actualDtos.get(0)).isEqualTo(postDto);

        verify(postRepository, times(1))
                .findAllByWritableTrue(eq(cond.getKeyword()), eq(pageable));
        verify(postMapper, times(1)).toPostDtoListFromPage(postsPage);
    }
}
