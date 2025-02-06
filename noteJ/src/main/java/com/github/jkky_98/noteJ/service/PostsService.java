package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.Post;
import com.github.jkky_98.noteJ.domain.Series;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.repository.PostRepository;
import com.github.jkky_98.noteJ.web.controller.dto.*;
import com.github.jkky_98.noteJ.web.controller.form.PostsConditionForm;
import com.github.jkky_98.noteJ.web.controller.form.ProfileForm;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostsService {

    private final PostRepository postRepository;
    private final UserService userService;
    private final ProfileService profileService;
    private final TagService tagService;
    private final FollowService followService;

    @Transactional(readOnly = true)
    public List<PostDto> getPosts(Long userId, PostsConditionForm cond) {
        User userFind = userService.findUserById(userId);

        List<Post> posts = postRepository.searchPosts(cond, userFind.getId());

        return posts.stream()
                .map(post -> PostDto.of(post, userFind))
                .toList();
    }

    @Transactional
    public List<PostDto> getPostsWithPageable(String username, PostsConditionForm cond, Pageable pageable) {
        User userFind = userService.findUserByUsername(username);
        Page<Post> posts = postRepository.searchPostsWithPage(cond, userFind.getId(), pageable);

        return posts.stream()
                .map(post -> PostDto.of(post, userFind))
                .toList();
    }


    @Transactional(readOnly = true)
    public PostsForm getPosts(GetPostsToServiceDto dto) {

        User userPost = userService.findUserByUsername(dto.getUsernamePost());

        ProfileForm profile = profileService.getProfile(userPost.getId());
        List<TagCountDto> allTag = tagService.getAllTag(userPost.getId());
        boolean following = followService.isFollowing(dto.getUser(), userPost);
        List<PostDto> posts = getPosts(userPost.getId(), dto.getCondition());

        return PostsForm.ofPosts(
                profile,
                posts,
                allTag,
                following,
                dto.getUsernamePost()
        );
    }

    @Transactional(readOnly = true)
    public PostsForm getSeriesTabs(String username, Optional<User> sessionUser) {
        User userFind = userService.findUserByUsername(username);

        // User의 Series 목록 가져오기
        List<Series> seriesList = userFind.getSeriesList();

        return PostsForm.ofSeries(
                profileService.getProfile(userFind.getId()),
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
                        userFind
                ),
                username
        );
    }

    @Transactional(readOnly = true)
    public List<PostNotOpenDto> getPostsNotOpen(User sessionUser) {
        User userFind = userService.findUserById(sessionUser.getId());

        List<Post> postAllNotOpen = postRepository.findAllByUserIdAndWritableFalse(userFind.getId());

        return postAllNotOpen.stream()
                .map(PostNotOpenDto::of)
                .toList();
    }
}
