package com.github.jkky_98.noteJ.web.controller.dto;

import com.github.jkky_98.noteJ.web.controller.form.ProfileForm;
import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PostsSeriesViewDto {
    private ProfileForm profileForm;
    private List<TagCountDto> tags;
    private boolean followStatus;
    private String username;
    private List<SeriesViewDto> seriesViews;

    public static PostsSeriesViewDto of(ProfileForm profileForm,
                                        List<TagCountDto> tags,
                                        boolean followStatus,
                                        String username,
                                        List<SeriesViewDto> seriesView) {
        return PostsSeriesViewDto.builder()
                .profileForm(profileForm)
                .tags(tags)
                .followStatus(followStatus)
                .username(username)
                .seriesViews(seriesView)
                .build();
    }
}
