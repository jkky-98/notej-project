package com.github.jkky_98.noteJ.web.controller.dto;

import com.github.jkky_98.noteJ.domain.user.User;
import lombok.*;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LikeListByPostDto {
    private String usernameLike;
    private String profilePicLike;
    private String userDescLike;

    public static LikeListByPostDto ofFromUser(User user) {
        return LikeListByPostDto.builder()
                .usernameLike(user.getUsername())
                .userDescLike(user.getUserDesc().getDescription())
                .profilePicLike(user.getUserDesc().getProfilePic())
                .build();
    }
}
