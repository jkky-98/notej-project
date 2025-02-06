package com.github.jkky_98.noteJ.repository.post;

import com.github.jkky_98.noteJ.domain.Post;
import com.github.jkky_98.noteJ.web.controller.form.PostsConditionForm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PostRepositoryCustom {

    List<Post> searchPosts(PostsConditionForm form, Long userId);
    Page<Post> searchPostsWithPage(PostsConditionForm form, Long userId, Pageable pageable);
}
