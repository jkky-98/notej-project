package com.github.jkky_98.noteJ.web.controller.dto;

import com.github.jkky_98.noteJ.web.controller.form.ProfileForm;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PostsViewDto {
    private ProfileForm profileForm;
    private List<PostDto> posts;
    private List<TagCountDto> tags;
    private boolean followStatus;
    private String username;
    private List<SeriesViewDto> seriesList;

    public static PostsViewDto ofPosts(ProfileForm profileForm, List<PostDto> posts, List<TagCountDto> tagAlls, boolean followStatus, String username) {
        return PostsViewDto.builder()
                .profileForm(profileForm)
                .posts(posts)
                .tags(tagAlls)
                .followStatus(followStatus)
                .username(username)
                .build();
    }

    public static PostsViewDto ofSeries(ProfileForm profileForm, List<SeriesViewDto> seriesList, boolean followStatus, String username) {
        return PostsViewDto.builder()
                .profileForm(profileForm)
                .seriesList(seriesList)
                .followStatus(followStatus)
                .username(username)
                .build();
    }
}
