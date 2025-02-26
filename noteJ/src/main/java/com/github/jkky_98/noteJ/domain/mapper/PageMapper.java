package com.github.jkky_98.noteJ.domain.mapper;

import com.github.jkky_98.noteJ.web.controller.dto.PostDto;
import com.github.jkky_98.noteJ.web.controller.dto.PostsForm;
import com.github.jkky_98.noteJ.web.controller.dto.SeriesViewDto;
import com.github.jkky_98.noteJ.web.controller.dto.TagCountDto;
import com.github.jkky_98.noteJ.web.controller.form.ProfileForm;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface PageMapper {

    @Mapping(source = "profile", target = "profileForm")
    @Mapping(source = "posts", target = "posts")
    @Mapping(source = "allTag", target = "tags")
    @Mapping(source = "canFollowing", target = "followStatus")
    @Mapping(source = "username", target = "username")
    PostsForm toPostsForm(ProfileForm profile, List<TagCountDto> allTag, boolean canFollowing, List<PostDto> posts, String username);

    @Mapping(source = "profile", target = "profileForm")
    @Mapping(source = "allTag", target = "tags")
    @Mapping(source = "canFollowing", target = "followStatus")
    @Mapping(source = "seriesViewDtoList", target = "seriesList")
    @Mapping(source = "username", target = "username")
    PostsForm toPostsFormSeriesPage(ProfileForm profile, List<TagCountDto> allTag, boolean canFollowing, List<SeriesViewDto> seriesViewDtoList, String username);
}
