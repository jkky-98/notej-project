package com.github.jkky_98.noteJ.web.controller.form;

import com.github.jkky_98.noteJ.domain.Post;
import com.github.jkky_98.noteJ.domain.Series;
import com.github.jkky_98.noteJ.domain.user.User;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class WriteForm {
    @NotBlank(message = "제목을 입력하지 않았습니다.")
    private String title;
    private List<String> tags;
    private String content;
    private MultipartFile thumbnail;
    private String postSummary;
    private boolean open;
    private String url;
    private String series;
    private List<String> seriesList = new ArrayList<>();

    public static WriteForm of(Post post, User user) {
        return WriteForm.builder()
                .title(post.getTitle())
                .tags(post.getPostTags().stream()
                        .map(postTag -> postTag.getTag().getName())
                        .toList())
                .content(post.getContent())
                .open(post.getWritable())
                .url(post.getPostUrl())
                .series(post.getSeries().getSeriesName())
                .seriesList(
                        user.getSeriesList().stream()
                                .map(Series::getSeriesName)
                                .toList()
                )
                .build();
    }
}
