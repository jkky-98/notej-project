package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.Follow;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.repository.FollowRepository;
import com.github.jkky_98.noteJ.web.controller.dto.FollowListPostProfileDto;
import com.github.jkky_98.noteJ.web.controller.dto.FollowerListViewDto;
import com.github.jkky_98.noteJ.web.controller.dto.FollowingListViewDto;
import com.github.jkky_98.noteJ.web.controller.form.FollowerListForm;
import com.github.jkky_98.noteJ.web.controller.form.FollowingListForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FollowService {
    
    private final FollowRepository followRepository;
    private final NotificationService notificationService;
    private final UserService userService;

    @Transactional(readOnly = true)
    public FollowingListForm getFollowingList(String usernameBlog) {
        User userBlog = userService.findUserByUsername(usernameBlog);

        FollowingListForm followingListForm = new FollowingListForm();

        fillUsersFollowingListIntoFollowingListForm(
                userBlog, followingListForm
        );

        followingListForm.setProfilePostUser(
                FollowListPostProfileDto.ofFollowing(userBlog)
        );

        return followingListForm;
    }

    @Transactional
    public void follow(String myUsername, String myFollowingUsername) {
        User userMe = userService.findUserByUsername(myUsername);
        User userMyFollowing = userService.findUserByUsername(myFollowingUsername);
        // 검증 및 Follow 엔티티 생성
        Follow follow = validFollowUser(userMe, userMyFollowing);
        // 저장
        followRepository.save(follow);
        // 팔로우 당한 상대방에 팔로우 알림 보내기
        notificationService.sendFollowNotification(userMyFollowing, userMe);
    }

    private Follow validFollowUser(User userMe, User targetUser) {
        // 자기 자신을 팔로우하는 경우 예외 처리
        if (userMe.getId().equals(targetUser.getId())) {
            throw new IllegalArgumentException("자기 자신을 팔로우할 수 없습니다.");
        }

        // 이미 팔로우한 사용자 체크
        boolean alreadyFollowing = userMe.getFollowingList().stream()
                .anyMatch(follow -> follow.getFollowing().equals(targetUser));

        if (alreadyFollowing) {
            throw new IllegalArgumentException("이미 팔로우한 사용자입니다.");
        }

        // 팔로우 객체 생성
        Follow follow = Follow.of(userMe, targetUser);

        // 현재 사용자의 followingList에 추가
        userMe.getFollowingList().add(follow);
        // 상대방 사용자의 followerList에 추가
        targetUser.getFollowerList().add(follow);

        return follow;
    }

    @Transactional(readOnly = true)
    public boolean isFollowing(Optional<User> sessionUser, String myFollowingUsername) {

        User userMyFollowing = userService.findUserByUsername(myFollowingUsername);

        // 세션 사용자와의 관계 확인
        return sessionUser
                .map(user -> checkIfUserIsFollowing(user, userMyFollowing))
                .orElse(false);
    }

    @Transactional
    public void unfollow(String sessionUsername, String myFollowingUsername) {
        User sessionUser = userService.findUserByUsername(sessionUsername);
        User userMyFollowing = userService.findUserByUsername(myFollowingUsername);

        Follow follow = sessionUser.getFollowingList()
                .stream()
                .filter(
                        myFollowing -> myFollowing.matchFollowing(userMyFollowing)
                )
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException("요청에 대한 팔로잉 관계를 찾을 수 없습니다.")
                );

        sessionUser.getFollowingList().remove(follow);
        userMyFollowing.getFollowerList().remove(follow);
    }

    // 세션 유저가 특정 사용자를 팔로우하고 있는지 확인
    private boolean checkIfUserIsFollowing(User sessionUser, User userMyFollowing) {
        // 세션 유저의 정보 다시 조회
        User userFind = userService.findUserById(sessionUser.getId());

        // 팔로잉 리스트에서 특정 사용자와의 관계 확인
        return userFind.getFollowingList().stream()
                .anyMatch(follow -> follow.matchFollowing(userMyFollowing));
    }

    @Transactional(readOnly = true)
    public FollowerListForm getFollowerList(String usernameBlog) {
        User userBlog = userService.findUserByUsername(usernameBlog);
        FollowerListForm followerListForm = new FollowerListForm();

        fillUsersFollowerListIntoFollwerListForm(
                userBlog, followerListForm
        );

        followerListForm.setProfilePostUser(
                FollowListPostProfileDto.ofFollower(userBlog)
        );

        return followerListForm;
    }

    private static void fillUsersFollowerListIntoFollwerListForm(User userBlog, FollowerListForm followerListForm) {
        userBlog.getFollowerList().stream()
                .map(Follow::getFollower)
                .forEach(follower -> {
                    followerListForm.getFollowers().add(FollowerListViewDto.of(follower));
                });
    }

    private static void fillUsersFollowingListIntoFollowingListForm(User userBlog, FollowingListForm followingListForm) {
        userBlog.getFollowingList().stream()
                .map(Follow::getFollowing)
                .forEach(follwing -> {
                    followingListForm.getFollowings().add(FollowingListViewDto.of(follwing));
                });
    }
}
