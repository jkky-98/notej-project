package com.github.jkky_98.noteJ.repository.post;

import com.github.jkky_98.noteJ.domain.Post;
import com.github.jkky_98.noteJ.web.controller.form.PostsConditionForm;

import java.util.List;

public interface PostRepositoryCustom {

    List<Post> searchPost(PostsConditionForm form);
}
