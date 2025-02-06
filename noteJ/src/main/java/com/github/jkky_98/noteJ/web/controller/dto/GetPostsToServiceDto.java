package com.github.jkky_98.noteJ.web.controller.dto;

import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.web.controller.form.PostsConditionForm;
import lombok.Data;

import java.util.Optional;

@Data
public class GetPostsToServiceDto {
    private String usernamePost;
    private PostsConditionForm condition;
    private Optional<User> user;

    public static GetPostsToServiceDto of(String usernamePost, PostsConditionForm condition, Optional<User> user) {
        GetPostsToServiceDto dto = new GetPostsToServiceDto();
        dto.setUsernamePost(usernamePost);
        dto.setCondition(condition);
        dto.setUser(user);
        return dto;
    }
}
