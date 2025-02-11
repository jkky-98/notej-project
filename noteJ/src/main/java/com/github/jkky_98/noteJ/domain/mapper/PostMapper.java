package com.github.jkky_98.noteJ.domain.mapper;

import com.github.jkky_98.noteJ.domain.Post;
import com.github.jkky_98.noteJ.domain.Series;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.web.controller.dto.AutoEditPostResponse;
import com.github.jkky_98.noteJ.web.controller.dto.AutoSavePostRequest;
import com.github.jkky_98.noteJ.web.controller.dto.AutoSavePostResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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
}

