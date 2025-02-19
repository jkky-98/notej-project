package com.github.jkky_98.noteJ.domain.mapper;

import com.github.jkky_98.noteJ.domain.Follow;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.web.controller.dto.FollowListPostProfileForm;
import com.github.jkky_98.noteJ.web.controller.dto.FollowingListViewForm;
import com.github.jkky_98.noteJ.web.controller.dto.FollowerListViewForm;
import com.github.jkky_98.noteJ.web.controller.form.FollowingListForm;
import com.github.jkky_98.noteJ.web.controller.form.FollowerListForm;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface FollowMapper {

    // ===== Following 관련 매핑 =====

    @Mapping(target = "followingUserUsername", source = "username")
    @Mapping(target = "followingUserDescription", source = "userDesc.description")
    @Mapping(target = "followingUserProfilePic", source = "userDesc.profilePic")
    FollowingListViewForm toFollowingListViewForm(User user);

    @Mapping(target = "username", source = "username")
    @Mapping(target = "profilePic", source = "userDesc.profilePic")
    @Mapping(target = "followingCount", expression = "java(user.getFollowingList() != null ? user.getFollowingList().size() : 0)")
    FollowListPostProfileForm toFollowListPostProfileFormForFollowing(User user);

    @Mapping(target = "followings", expression = "java(mapFollowsToFollowingListViewForms(user.getFollowingList()))")
    @Mapping(target = "profilePostUser", expression = "java(toFollowListPostProfileFormForFollowing(user))")
    FollowingListForm toFollowingListForm(User user);

    default List<FollowingListViewForm> mapFollowsToFollowingListViewForms(List<Follow> follows) {
        if (follows == null) {
            return new ArrayList<>();
        }
        return follows.stream()
                .map(follow -> toFollowingListViewForm(follow.getFollowing()))
                .toList();
    }

    // ===== Follower 관련 매핑 =====

    @Mapping(target = "followerUserUsername", source = "username")
    @Mapping(target = "followerUserDescription", source = "userDesc.description")
    @Mapping(target = "followerUserProfilePic", source = "userDesc.profilePic")
    // followStatus 기본값을 false로 설정
    @Mapping(target = "followStatus", constant = "false")
    FollowerListViewForm toFollowerListViewForm(User user);

    @Mapping(target = "username", source = "username")
    @Mapping(target = "profilePic", source = "userDesc.profilePic")
    @Mapping(target = "followerCount", expression = "java(user.getFollowerList() != null ? user.getFollowerList().size() : 0)")
    FollowListPostProfileForm toFollowListPostProfileFormForFollower(User user);

    @Mapping(target = "followers", expression = "java(mapFollowsToFollowerListViewForms(user.getFollowerList()))")
    @Mapping(target = "profilePostUser", expression = "java(toFollowListPostProfileFormForFollower(user))")
    FollowerListForm toFollowerListForm(User user);

    default List<FollowerListViewForm> mapFollowsToFollowerListViewForms(List<Follow> follows) {
        if (follows == null) {
            return new ArrayList<>();
        }
        return follows.stream()
                .map(follow -> toFollowerListViewForm(follow.getFollower()))
                .toList();
    }
}
