package com.github.jkky_98.noteJ.domain.mapper;

import com.github.jkky_98.noteJ.domain.*;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.service.util.DefaultConst;
import com.github.jkky_98.noteJ.web.controller.dto.*;
import com.github.jkky_98.noteJ.web.controller.form.WriteForm;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", uses = {CommentMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface PostMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "series", target = "series")
    @Mapping(source = "request.content", target = "content", qualifiedByName = "decodeContent")
    @Mapping(source = "request.title", target = "postUrl", qualifiedByName = "generatePostUrl")
    @Mapping(target = "writable", constant = "false")
    Post toPostForAutoSave(AutoSavePostRequest request, User user, Series series);

    @Mapping(source = "post.user.username", target = "username")
    @Mapping(source = "post.postUrl", target = "postUrl")
    AutoSavePostResponse toAutoSaveResponse(Post post);

    @Mapping(source = "post.postUrl", target = "postUrl")
    AutoEditPostResponse toAutoEditResponse(Post post);

    @Named("decodeContent")
    default String decodeContent(String content) {
        return URLDecoder.decode(content, StandardCharsets.UTF_8);
    }

    @Named("generatePostUrl")
    default String generatePostUrl(String title) {
        return title + UUID.randomUUID();
    }

    @Mapping(source = "post.title", target = "title")
    @Mapping(source = "post.content", target = "content")
    @Mapping(source = "post.writable", target = "open")
    @Mapping(source = "post.series.seriesName", target = "series", defaultValue = "")
    @Mapping(source = "post", target = "tags", qualifiedByName = "mapTags")
    @Mapping(source = "user", target = "seriesList", qualifiedByName = "mapSeriesList")
    @Mapping(target = "thumbnail", ignore = true)
    WriteForm toWriteForm(Post post, User user);

    @Named("mapTags")
    default List<String> mapTags(Post post) {
        return post.getPostTags().stream()
                .map(postTag -> postTag.getTag().getName())
                .toList();
    }

    @Named("mapSeriesList")
    default List<String> mapSeriesList(User user) {
        return user.getSeriesList().stream()
                .map(Series::getSeriesName)
                .toList();
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "form.title", target = "title")
    @Mapping(source = "form.content", target = "content", qualifiedByName = "decodeContent")
    @Mapping(source = "form.open", target = "writable")
    @Mapping(source = "form.postSummary", target = "postSummary")
    @Mapping(source = "form.title", target = "postUrl", qualifiedByName = "generatePostUrl")
    @Mapping(source = "series", target = "series")
    @Mapping(source = "thumbnail", target = "thumbnail")
    @Mapping(source = "user", target = "user")
    Post toPostSaveWrite(WriteForm form, User user, Series series, String thumbnail);

    @Mapping(source = "post.title", target = "title")
    @Mapping(source = "post.postSummary", target = "postSummary")
    @Mapping(source = "post.postUrl", target = "postUrl")
    @Mapping(source = "post.thumbnail", target = "thumbnail")
    @Mapping(source = "post.writable", target = "writable")
    @Mapping(source = "post.createDt", target = "createByDt")
    @Mapping(source = "post.user.username", target = "username")
    @Mapping(source = "post.postTags", target = "tags", qualifiedByName = "getTagNamesFromPostTags")
    @Mapping(expression = "java(post.getComments().size())", target = "commentCount")
    @Mapping(expression = "java(post.getLikes().size())", target = "likeCount")
    PostDto toPostDto(Post post);

    List<PostDto> toPostDtoList(List<Post> posts);

    @Named("mapPageToList")
    default List<PostDto> toPostDtoListFromPage(Page<Post> posts) {
        return toPostDtoList(posts.getContent()); // 기존 List 변환 메서드 사용
    }

    @Named("getTagNamesFromPostTags")
    default List<String> getTagNamesFromPostTags(List<PostTag> postTags) {
        return postTags.stream()
                .map(postTag -> postTag.getTag().getName())
                .toList();
    }

    @Mapping(source = "post.title", target = "title")
    @Mapping(source = "post.postUrl", target = "postUrl")
    @Mapping(source = "post.createDt", target = "createDt")
    @Mapping(source = "post.postSummary", target = "postSummary")
    PostNotOpenDto toPostNotOpenDto(Post post);

    List<PostNotOpenDto> toPostNotOpenDtoList(List<Post> posts);

    @Mapping(source = "post.title", target = "title")
    @Mapping(source = "post.postUrl", target = "postUrl")
    @Mapping(source = "post.user.username", target = "username")
    @Mapping(source = "post.createDt", target = "createByDt")
    @Mapping(source = "post.postTags", target = "tags", qualifiedByName = "getTagNamesFromPostTags")
    @Mapping(source = "post.comments", target = "comments")
    @Mapping(expression = "java(post.getLikes().size())", target = "likeCount")
    PostViewDto toPostViewDto(Post post);
}

