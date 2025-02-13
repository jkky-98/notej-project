package com.github.jkky_98.noteJ.web.controller.dto;

import com.github.jkky_98.noteJ.web.controller.form.ProfileForm;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PostsForm {
    private ProfileForm profileForm;
    private List<PostDto> posts;
    private List<TagCountDto> tags;
    private boolean followStatus;
    private String username;
    private List<SeriesViewDto> seriesList;
}
