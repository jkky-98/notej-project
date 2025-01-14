package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.Post;
import com.github.jkky_98.noteJ.domain.Series;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.repository.PostRepository;
import com.github.jkky_98.noteJ.repository.SeriesRepository;
import com.github.jkky_98.noteJ.repository.UserRepository;
import com.github.jkky_98.noteJ.web.controller.dto.PostDto;
import com.github.jkky_98.noteJ.web.controller.dto.PostsSeriesViewDto;
import com.github.jkky_98.noteJ.web.controller.dto.PostsViewDto;
import com.github.jkky_98.noteJ.web.controller.form.PostsConditionForm;
import com.github.jkky_98.noteJ.web.controller.dto.SeriesViewDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostsService {

    private final UserRepository userRepository;
    private final SeriesRepository seriesRepository;
    private final PostRepository postRepository;
    private final UserService userService;
    private final ProfileService profileService;
    private final TagService tagService;
    private final FollowService followService;

    @Transactional
    public List<PostDto> getPosts(String username, PostsConditionForm cond) {
        User userFind = userService.findUserByUsername(username);

        List<Post> posts = postRepository.searchPosts(cond, username);

        return posts.stream()
                .map(post -> PostDto.of(post, userFind))
                .toList();
    }


    @Transactional(readOnly = true)
    public PostsViewDto getPostsViewDto(String username, PostsConditionForm postsConditionForm, Optional<User> sessionUser) {
        return PostsViewDto.ofPosts(
                profileService.getProfile(username),
                getPosts(username, postsConditionForm),
                tagService.getAllTag(username),
                followService.isFollowing(sessionUser, username),
                username
        );
    }

    @Transactional(readOnly = true)
    public PostsViewDto getSeriesTabs(String username, Optional<User> sessionUser) {
        User userFind = userService.findUserByUsername(username);

        // User의 Series 목록 가져오기
        List<Series> seriesList = userFind.getSeriesList();

        return PostsViewDto.ofSeries(
                profileService.getProfile(username),
                seriesList.stream()
                        .map(series -> SeriesViewDto.of(
                                series.getSeriesName(),
                                series.getPosts().size(),
                                series.getPosts().isEmpty()
                                        ? null
                                        : series.getPosts().get(0).getLastModifiedDt()
                        ))
                        .toList(),
                followService.isFollowing(
                        sessionUser,
                        username
                ),
                username
        );
    }
}
