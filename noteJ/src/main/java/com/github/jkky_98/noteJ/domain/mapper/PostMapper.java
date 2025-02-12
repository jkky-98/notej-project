package com.github.jkky_98.noteJ.domain.mapper;

import com.github.jkky_98.noteJ.domain.Post;
import com.github.jkky_98.noteJ.domain.Series;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.service.util.DefaultConst;
import com.github.jkky_98.noteJ.web.controller.dto.AutoEditPostResponse;
import com.github.jkky_98.noteJ.web.controller.dto.AutoSavePostRequest;
import com.github.jkky_98.noteJ.web.controller.dto.AutoSavePostResponse;
import com.github.jkky_98.noteJ.web.controller.form.WriteForm;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
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
}

