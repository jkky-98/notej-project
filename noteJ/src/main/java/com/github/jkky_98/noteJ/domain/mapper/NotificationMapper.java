package com.github.jkky_98.noteJ.domain.mapper;

import com.github.jkky_98.noteJ.domain.Notification;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.web.controller.dto.NotificationForm;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface NotificationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "type", expression = "java(com.github.jkky_98.noteJ.domain.NotificationType.FOLLOW)")
    @Mapping(target = "message", expression = "java(userSendNotification.getUsername() + \"님으로부터 팔로우 되었습니다.\")")
    @Mapping(target = "status", constant = "false")
    @Mapping(target = "sender", source = "userSendNotification")
    @Mapping(target = "receiver", source = "userGetNotification")
    Notification toNotificationForFollow(User userSendNotification, User userGetNotification);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "type", expression = "java(com.github.jkky_98.noteJ.domain.NotificationType.LIKE)")
    @Mapping(target = "message", expression = "java(userSendNotification.getUsername() + \"님이 당신의 게시글 : \" + postTitle + \"에 좋아요를 눌렀습니다.\")")
    @Mapping(target = "status", constant = "false")
    @Mapping(target = "sender", source = "userSendNotification")
    @Mapping(target = "receiver", source = "userGetNotification")
    Notification toNotificationForLike(User userSendNotification, User userGetNotification, String postTitle);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "type", expression = "java(com.github.jkky_98.noteJ.domain.NotificationType.COMMENT)")
    @Mapping(target = "message", expression = "java(userSendNotification.getUsername() + \"님이 당신의 게시글 : \" + postTitle + \"에 댓글을 남겼습니다.\")")
    @Mapping(target = "status", constant = "false")
    @Mapping(target = "sender", source = "userSendNotification")
    @Mapping(target = "receiver", source = "userGetNotification")
    Notification toNotificationForComment(User userSendNotification, User userGetNotification, String postTitle);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "type", expression = "java(com.github.jkky_98.noteJ.domain.NotificationType.COMMENT)")
    @Mapping(target = "message", expression = "java(userSendNotification.getUsername() + \"님이 게시글 : \" + postTitle + \"의 당신의 댓글에 대댓글을 남겼습니다.\")")
    @Mapping(target = "status", constant = "false")
    @Mapping(target = "sender", source = "userSendNotification")
    @Mapping(target = "receiver", source = "userGetNotification")
    Notification toNotificationForCommentParents(User userSendNotification, User userGetNotification, String postTitle);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "message", source = "message")
    @Mapping(target = "type", expression = "java(notification.getType().name())")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "createTime", source = "createDt")
    NotificationForm toNotificationForm(Notification notification);

    // 리스트 매핑
    List<NotificationForm> toNotificationFormList(List<Notification> notifications);
}
