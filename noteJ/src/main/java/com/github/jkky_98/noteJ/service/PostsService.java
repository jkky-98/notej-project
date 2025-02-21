package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.Post;
import com.github.jkky_98.noteJ.domain.Series;
import com.github.jkky_98.noteJ.domain.mapper.PageMapper;
import com.github.jkky_98.noteJ.domain.mapper.PostMapper;
import com.github.jkky_98.noteJ.domain.mapper.SeriesMapper;
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
    private final PostMapper postMapper;
    private final PageMapper pageMapper;
    private final SeriesMapper seriesMapper;
    private final PostService postService;

    @Transactional(readOnly = true)
    public List<PostDto> getPosts(Long userId, PostsConditionForm cond) {
        User userFind = userService.findUserById(userId);

        List<Post> posts = postRepository.searchPosts(cond, userFind.getId());

        return postMapper.toPostDtoList(posts);
    }

    @Transactional(readOnly = true)
    public List<PostDto> getPostsWithPageable(String username, PostsConditionForm cond, Pageable pageable) {
        User userFind = userService.findUserByUsername(username);
        Page<Post> posts = postRepository.searchPostsWithPage(cond, userFind.getId(), pageable);

        return postMapper.toPostDtoListFromPage(posts);
    }


    @Transactional(readOnly = true)
    public PostsForm getPostsInitialPage(GetPostsToServiceDto dto) {

        User userPost = userService.findUserByUsername(dto.getUsernamePost());

        ProfileForm profile = profileService.getProfile(userPost.getId());
        List<TagCountDto> allTag = tagService.getAllTag(userPost.getId());
        boolean canFollowing = followService.isFollowing(dto.getUser(), userPost);
        List<PostDto> posts = getPosts(userPost.getId(), dto.getCondition());

        return pageMapper.toPostsForm(
                profile,
                allTag,
                canFollowing,
                posts,
                userPost.getUsername()
        );
    }

    @Transactional(readOnly = true)
    public PostsForm getSeriesTabs(String username, Optional<User> sessionUser) {
        User userPost = userService.findUserByUsername(username);

        ProfileForm profile = profileService.getProfile(userPost.getId());
        List<TagCountDto> allTag = tagService.getAllTag(userPost.getId());
        List<Series> seriesList = userPost.getSeriesList();
        List<SeriesViewDto> seriesViewDtoList = seriesMapper.toSeriesViewDtoList(seriesList);
        boolean canFollowing = followService.isFollowing(
                sessionUser,
                userPost
        );

        return pageMapper.toPostsFormSeriesPage(
                profile,
                allTag,
                canFollowing,
                seriesViewDtoList,
                username
        );
    }

    @Transactional(readOnly = true)
    public List<PostNotOpenDto> getPostsNotOpen(Long sessionUserId) {
        User userFind = userService.findUserById(sessionUserId);

        List<Post> postAllNotOpen = postRepository.findAllByUserIdAndWritableFalse(userFind.getId());

        return postMapper.toPostNotOpenDtoList(postAllNotOpen);
    }

    // toDo : need to Test
    @Transactional(readOnly = true)
    public List<PostDto> getPostsGlobalWithPaging(SearchGlobalCondition cond, Pageable pageable) {
        String searchKeyword = cond.getKeyword();

        Page<Post> postsPage = postRepository.findAllByWritableTrue(searchKeyword, pageable);

        return postMapper.toPostDtoListFromPage(postsPage);

    }
}
