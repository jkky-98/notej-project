package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.Follow;
import com.github.jkky_98.noteJ.domain.mapper.FollowMapper;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.repository.FollowRepository;
import com.github.jkky_98.noteJ.web.controller.form.FollowerListForm;
import com.github.jkky_98.noteJ.web.controller.form.FollowingListForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("[FollowService] Unit Tests")
class FollowServiceTest {

    @Mock
    private FollowRepository followRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserService userService;

    @Mock
    private FollowMapper followMapper;

    @InjectMocks
    private FollowService followService;

    // 테스트용 사용자 객체 (빌더 패턴 사용)
    private User userA; // 예: 팔로우 요청자
    private User userB; // 예: 팔로우 대상

    @BeforeEach
    void setUp() {
        // 팔로워/팔로잉 리스트는 빈 ArrayList로 초기화 (테스트 목적)
        userA = User.builder()
                .id(1L)
                .username("userA")
                .followerList(new ArrayList<>())
                .followingList(new ArrayList<>())
                .build();

        userB = User.builder()
                .id(2L)
                .username("userB")
                .followerList(new ArrayList<>())
                .followingList(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("[FollowService] getFollowingList: 정상 조회")
    void testGetFollowingList() {
        // given
        String usernameBlog = "userA";
        FollowingListForm followingListForm = new FollowingListForm();
        // stub: userService 및 followMapper 동작 설정
        when(userService.findUserByUsername(usernameBlog)).thenReturn(userA);
        when(followMapper.toFollowingListForm(userA)).thenReturn(followingListForm);

        // when
        FollowingListForm result = followService.getFollowingList(usernameBlog);

        // then
        assertNotNull(result);
        verify(userService).findUserByUsername(usernameBlog);
        verify(followMapper).toFollowingListForm(userA);
    }

    @Test
    @DisplayName("[FollowService] follow: 정상 팔로우")
    void testFollow() {
        // given
        String myUsername = "userA";
        String myFollowingUsername = "userB";
        when(userService.findUserByUsername(myUsername)).thenReturn(userA);
        when(userService.findUserByUsername(myFollowingUsername)).thenReturn(userB);

        // 팔로우 객체 생성은 validFollowUser 내부에서 일어나므로, 실제 객체의 생성은 호출 후 userA, userB의 리스트에 추가됨.
        // when(validFollowUser(..))는 private 메서드이므로, 실제 동작을 검증하기 위해 followRepository.save 호출 후 알림 전송을 검증합니다.
        // when
        followService.follow(myUsername, myFollowingUsername);

        // then
        // followRepository.save가 호출되어야 함.
        verify(followRepository).save(any(Follow.class));
        // 팔로우 당한 사용자(userB)에게 알림 전송
        verify(notificationService).sendFollowNotification(eq(userB), eq(userA));
        // 또한, validFollowUser에 의해 양쪽 리스트에 follow 객체가 추가되었는지 확인 (간단히 사이즈 검증)
        assertEquals(1, userA.getFollowingList().size());
        assertEquals(1, userB.getFollowerList().size());
    }

    @Test
    @DisplayName("[FollowService] isFollowing: 팔로우 상태 확인 (true)")
    void testIsFollowingTrue() {
        // given
        // userA가 userB를 팔로우하는 상태
        Follow follow = Follow.of(userA, userB);
        userA.getFollowingList().add(follow);
        when(userService.findUserById(userA.getId())).thenReturn(userA);

        // when
        boolean result = followService.isFollowing(Optional.of(userA), userB);

        // then
        assertTrue(result);
    }

    @Test
    @DisplayName("[FollowService] isFollowing: 팔로우 상태 확인 (false)")
    void testIsFollowingFalse() {
        // given: userA가 userB를 팔로우하지 않은 경우
        when(userService.findUserById(userA.getId())).thenReturn(userA);

        // when
        boolean result = followService.isFollowing(Optional.of(userA), userB);

        // then
        assertFalse(result);
    }

    @Test
    @DisplayName("[FollowService] isFollowing: 세션 사용자가 없는 경우 false 반환")
    void testIsFollowingNoSessionUser() {
        // when
        boolean result = followService.isFollowing(Optional.empty(), userB);

        // then
        assertFalse(result);
    }

    @Test
    @DisplayName("[FollowService] unfollow: 정상 언팔로우")
    void testUnfollow() {
        // given
        // userA가 userB를 이미 팔로우한 상태
        Follow follow = Follow.of(userA, userB);
        userA.getFollowingList().add(follow);
        userB.getFollowerList().add(follow);
        when(userService.findUserByUsername("userA")).thenReturn(userA);
        when(userService.findUserByUsername("userB")).thenReturn(userB);

        // when
        followService.unfollow("userA", "userB");

        // then
        // 양쪽 리스트에서 follow 객체가 제거되어야 함.
        assertFalse(userA.getFollowingList().contains(follow));
        assertFalse(userB.getFollowerList().contains(follow));
    }

    @Test
    @DisplayName("[FollowService] getFollowerList: 정상 조회")
    void testGetFollowerList() {
        // given
        String usernameBlog = "userA";
        FollowerListForm followerListForm = new FollowerListForm();
        when(userService.findUserByUsername(usernameBlog)).thenReturn(userA);
        when(followMapper.toFollowerListForm(userA)).thenReturn(followerListForm);

        // when
        FollowerListForm result = followService.getFollowerList(usernameBlog);

        // then
        assertNotNull(result);
        verify(userService).findUserByUsername(usernameBlog);
        verify(followMapper).toFollowerListForm(userA);
    }
}
