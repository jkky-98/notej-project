package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.Post;
import com.github.jkky_98.noteJ.domain.PostTag;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.repository.UserRepository;
import com.github.jkky_98.noteJ.web.controller.dto.PostDto;
import com.github.jkky_98.noteJ.web.controller.dto.TagCountDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PostsService {

    private final UserRepository userRepository;

    public List<PostDto> getPosts(String username) {
        User userFind = userRepository.findByUsername(username).orElseThrow(() -> new EntityNotFoundException("User not found"));

        List<Post> posts = userFind.getPosts();
        List<PostDto> postDtos = new ArrayList<>();
        for (Post post : posts) {
            PostDto postDto = new PostDto();
            postDto.setTitle(post.getTitle());
            postDto.setPostSummary(post.getPostSummary());
            postDto.setPostUrl(post.getPostUrl());
            postDto.setThumbnail(post.getThumbnail());
            postDto.setWritable(post.getWritable());
            postDto.setCreateByDt(post.getCreateDt());
            postDto.setUsername(username);

            List<PostTag> postTags = post.getPostTags();
            for (PostTag postTag : postTags) {
                postDto.getTags().add(postTag.getTag().getName());
            }

            postDto.setCommentCount(post.getComments().size());
            postDto.setLikeCount(post.getLikes().size());

            postDtos.add(postDto);
        }

        return postDtos;
    }

    public List<TagCountDto> getAllTag(String username) {
        User userFind = userRepository.findByUsername(username).orElseThrow(() -> new EntityNotFoundException("User not found"));

        return userRepository.findTagsByUser(userFind.getUsername());
    }

}
