package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.Post;
import com.github.jkky_98.noteJ.domain.PostTag;
import com.github.jkky_98.noteJ.domain.Series;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.repository.UserRepository;
import com.github.jkky_98.noteJ.web.controller.dto.PostDto;
import com.github.jkky_98.noteJ.web.controller.dto.TagCountDto;
import com.github.jkky_98.noteJ.web.controller.form.SeriesViewForm;
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

    public List<SeriesViewForm> getSeries(String username) {
        User userFind = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // User의 Series 목록 가져오기
        List<Series> seriesList = userFind.getSeriesList();

        // Series 데이터를 SeriesViewForm으로 변환
        List<SeriesViewForm> seriesViewForms = new ArrayList<>();
        for (Series series : seriesList) {
            int count = series.getPosts().size(); // Series에 포함된 글 개수
            SeriesViewForm seriesViewForm = new SeriesViewForm(
                    series.getSeriesName(),
                    count,
                    series.getPosts().get(0).getLastModifiedDt()
            );
            seriesViewForms.add(seriesViewForm);
        }
        return seriesViewForms;

    }

    public List<PostDto> getPostsForSeries(String username, String seriesName) {
        User userFind = userRepository.findByUsername(username).orElseThrow(() -> new EntityNotFoundException("User not found"));

        List<Post> posts = userFind.getPosts();
        List<PostDto> postDtos = new ArrayList<>();

        for (Post post : posts) {
            if (post.getSeries().getSeriesName().equals(seriesName)) {
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
        }

        return postDtos;
    }
}
