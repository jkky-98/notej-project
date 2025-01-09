package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.Follow;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.repository.FollowRepository;
import com.github.jkky_98.noteJ.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FollowService {
    
    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    @Transactional
    public void follow(String sessionUsername, String getFollwingUsername) {
        User userFollowing = userRepository.findByUsername(sessionUsername).orElseThrow(() -> new EntityNotFoundException("follow를 신청한 User를 찾을 수 없습니다. 세션 로그인 유저와 일치할 것..."));
        User userGetFollowing = userRepository.findByUsername(getFollwingUsername).orElseThrow(() -> new EntityNotFoundException("follow를 당한 User를 찾을 수 없습니다"));
        // 검증 및 Follow 엔티티 생성
        Follow follow = validFollowUser(userFollowing, userGetFollowing);
        // 저장
        followRepository.save(follow);
    }

    private Follow validFollowUser(User userFollowing, User userGetFollowing) {
        // 자기 자신을 팔로우하는 경우 예외 처리
        if (this.equals(userGetFollowing)) {
            throw new IllegalArgumentException("자기 자신을 팔로우할 수 없습니다.");
        }

        // 이미 팔로우한 사용자 체크
        boolean alreadyFollowing = userFollowing.getFollowingList().stream()
                .anyMatch(follow -> follow.getFollowing().equals(userGetFollowing));

        if (alreadyFollowing) {
            throw new IllegalArgumentException("이미 팔로우한 사용자입니다.");
        }

        // 팔로우 객체 생성
        Follow follow = Follow.builder()
                .follower(userFollowing)
                .following(userGetFollowing)
                .build();

        // 현재 사용자의 followingList에 추가
        userFollowing.getFollowingList().add(follow);
        // 상대방 사용자의 followerList에 추가
        userGetFollowing.getFollowerList().add(follow);

        return follow;
    }

    @Transactional
    public boolean isFollowing(String sessionUsername, String getFollwingUsername) {
        User sessionUser = userRepository.findByUsername(sessionUsername).orElseThrow(() -> new EntityNotFoundException("sessionUser를 찾을 수 없습니다."));
        // 세션 유저의 팔로우 리스트에서 찾기
        return sessionUser.getFollowingList().stream()
                .anyMatch(follow -> follow.getFollowing().getUsername().equals(getFollwingUsername));
    }

    @Transactional
    public void unfollow(String sessionUsername, String getFollwingUsername) {
        User sessionUser = userRepository.findByUsername(sessionUsername).orElseThrow(() -> new EntityNotFoundException("sessionUser를 찾을 수 없습니다."));
        User userGetFollowing = userRepository.findByUsername(getFollwingUsername).orElseThrow(() -> new EntityNotFoundException("unfollow를 당한 User를 찾을 수 없습니다."));
        Follow follow = sessionUser.getFollowingList().stream()
                .filter(follow1 -> follow1.getFollowing().equals(userGetFollowing))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("follow를 찾을 수 없습니다."));

        sessionUser.getFollowingList().remove(follow);
        userGetFollowing.getFollowerList().remove(follow);
    }
}
