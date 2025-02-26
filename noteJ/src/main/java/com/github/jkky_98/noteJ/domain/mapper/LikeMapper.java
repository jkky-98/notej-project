package com.github.jkky_98.noteJ.domain.mapper;

import com.github.jkky_98.noteJ.domain.Like;
import com.github.jkky_98.noteJ.domain.Post;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.web.controller.dto.LikeListByPostDto;
import com.github.jkky_98.noteJ.web.controller.form.LikeCardForm;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface LikeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "user", target = "user")
    @Mapping(source = "post", target = "post")
    @Mapping(source = "post.user", target = "userGetLike")
    Like toLike(User user, Post post);

    @Mapping(source = "user.username", target = "usernameLike")
    @Mapping(source = "user.userDesc.description", target = "userDescLike")
    @Mapping(source = "user.userDesc.profilePic", target = "profilePicLike")
    LikeListByPostDto toLikeListByPostDto(User user);

    @Mapping(source = "post.id", target = "postId")
    @Mapping(source = "post.title", target = "postTitle")
    @Mapping(source = "post.postSummary", target = "postDescription")
    @Mapping(source = "post", target = "postUrl", qualifiedByName = "generatePostUrl")
    @Mapping(source = "post.thumbnail", target = "postThumbnailUrl")
    LikeCardForm toLikeCardFormByPost(Post post);

    @Named("generatePostUrl")
    default String generatePostUrl(Post post) {
        String username = post.getUser().getUsername();
        String postUrl = post.getPostUrl();

        return "/@" + username + "/post/" + postUrl;
    }
}
