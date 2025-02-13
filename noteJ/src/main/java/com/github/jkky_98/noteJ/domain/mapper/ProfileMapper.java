package com.github.jkky_98.noteJ.domain.mapper;

import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.web.controller.form.ProfileForm;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface ProfileMapper {

    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "user.userDesc.profilePic", target = "profilePic")
    @Mapping(source = "user.userDesc.description", target = "description")
    @Mapping(source = "user.userDesc.socialEmail", target = "socialEmail")
    @Mapping(source = "user.userDesc.socialGitHub", target = "socialGitHub")
    @Mapping(source = "user.userDesc.socialTwitter", target = "socialTwitter")
    @Mapping(source = "user.userDesc.socialFacebook", target = "socialFacebook")
    @Mapping(source = "user.userDesc.socialOther", target = "socialOther")
    @Mapping(source = "user", target = "followings", qualifiedByName = "countFollowings")
    @Mapping(source = "user", target = "followers", qualifiedByName = "countFollowers")
    ProfileForm toProfileForm(User user);

    // ✅ 팔로잉 수 변환
    @Named("countFollowings")
    default int countFollowings(User user) {
        return user.getFollowingList() != null ? user.getFollowingList().size() : 0;
    }

    // ✅ 팔로워 수 변환
    @Named("countFollowers")
    default int countFollowers(User user) {
        return user.getFollowerList() != null ? user.getFollowerList().size() : 0;
    }
}
