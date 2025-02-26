package com.github.jkky_98.noteJ.domain.mapper;

import com.github.jkky_98.noteJ.domain.Comment;
import com.github.jkky_98.noteJ.domain.Post;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.web.controller.dto.CommentsForm;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.Optional;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface CommentMapper {

    @Mapping(source = "createBy", target = "createBy")
    @Mapping(source = "createDt", target = "createByDt")
    @Mapping(source = "content", target = "content")
    @Mapping(source = "parent.id", target = "parentsId")
    @Mapping(source = "childrens", target = "childrens", qualifiedByName = "mapChildrens")
    CommentsForm toCommentsForm(Comment comment);

    List<CommentsForm> toCommentsFormList(List<Comment> comments);

    // 대댓글 변환 (재귀적 변환)
    @Named("mapChildrens")
    default List<CommentsForm> mapChildrens(List<Comment> childrens) {
        return childrens != null ? toCommentsFormList(childrens) : List.of();
    }
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "post", target = "post")
    @Mapping(source = "user", target = "user")
    @Mapping(source = "content", target = "content")
    @Mapping(target = "parent", expression = "java(parent.orElse(null))")
    Comment toCommentForSave(Post post, User user, String content, Optional<Comment> parent);
}

