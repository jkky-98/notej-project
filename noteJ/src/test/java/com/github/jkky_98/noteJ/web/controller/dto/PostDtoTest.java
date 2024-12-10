package com.github.jkky_98.noteJ.web.controller.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class PostDtoTest {


    @Test
    @DisplayName("[PostDto] 객체 생성 및 기본 상태 테스트")
    void postDtoCreationAndDefaultStateTest() {
        // given
        String title = "Test Title";
        String postSummary = "This is a test summary";
        String postUrl = "http://example.com/test-post";
        String thumbnail = "http://example.com/test-thumbnail.jpg";
        boolean writable = true;
        int commentCount = 10;
        int likeCount = 25;
        LocalDateTime createByDt = LocalDateTime.now();

        // when
        PostDto postDto = new PostDto();
        postDto.setTitle(title);
        postDto.setPostSummary(postSummary);
        postDto.setPostUrl(postUrl);
        postDto.setThumbnail(thumbnail);
        postDto.setWritable(writable);
        postDto.setTags(Arrays.asList("tag1", "tag2"));
        postDto.setCommentCount(commentCount);
        postDto.setLikeCount(likeCount);
        postDto.setCreateByDt(createByDt);

        // then
        assertThat(postDto.getTitle()).isEqualTo(title);
        assertThat(postDto.getPostSummary()).isEqualTo(postSummary);
        assertThat(postDto.getPostUrl()).isEqualTo(postUrl);
        assertThat(postDto.getThumbnail()).isEqualTo(thumbnail);
        assertThat(postDto.isWritable()).isEqualTo(writable);
        assertThat(postDto.getTags()).containsExactly("tag1", "tag2");
        assertThat(postDto.getCommentCount()).isEqualTo(commentCount);
        assertThat(postDto.getLikeCount()).isEqualTo(likeCount);
        assertThat(postDto.getCreateByDt()).isEqualTo(createByDt);
    }

    @Test
    @DisplayName("[PostDto] equals 및 hashCode 테스트")
    void postDtoEqualsAndHashCodeTest() {
        // given
        PostDto postDto1 = new PostDto();
        postDto1.setTitle("Test Title");
        postDto1.setPostSummary("This is a test summary");
        postDto1.setPostUrl("http://example.com/test-post");
        postDto1.setThumbnail("http://example.com/test-thumbnail.jpg");
        postDto1.setWritable(true);
        postDto1.setTags(Arrays.asList("tag1", "tag2"));
        postDto1.setCommentCount(10);
        postDto1.setLikeCount(25);
        postDto1.setCreateByDt(LocalDateTime.now());

        PostDto postDto2 = new PostDto();
        postDto2.setTitle("Test Title");
        postDto2.setPostSummary("This is a test summary");
        postDto2.setPostUrl("http://example.com/test-post");
        postDto2.setThumbnail("http://example.com/test-thumbnail.jpg");
        postDto2.setWritable(true);
        postDto2.setTags(Arrays.asList("tag1", "tag2"));
        postDto2.setCommentCount(10);
        postDto2.setLikeCount(25);
        postDto2.setCreateByDt(postDto1.getCreateByDt()); // 동일한 시간 설정

        // then
        assertThat(postDto1).isEqualTo(postDto2);
        assertThat(postDto1.hashCode()).isEqualTo(postDto2.hashCode());
    }

    @Test
    @DisplayName("[PostDto] 기본 상태 테스트")
    void postDtoDefaultInitializationTest() {
        // when
        PostDto postDto = new PostDto();

        // then
        assertThat(postDto.getTitle()).isNull();
        assertThat(postDto.getPostSummary()).isNull();
        assertThat(postDto.getPostUrl()).isNull();
        assertThat(postDto.getThumbnail()).isNull();
        assertThat(postDto.isWritable()).isFalse();
        assertThat(postDto.getTags()).isEmpty();
        assertThat(postDto.getCommentCount()).isZero();
        assertThat(postDto.getLikeCount()).isZero();
        assertThat(postDto.getCreateByDt()).isNull();
    }
}
