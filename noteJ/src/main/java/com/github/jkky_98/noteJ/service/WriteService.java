package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.*;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.file.FileStore;
import com.github.jkky_98.noteJ.repository.*;
import com.github.jkky_98.noteJ.web.controller.dto.WriteDto;
import com.github.jkky_98.noteJ.web.controller.form.WriteForm;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WriteService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final SeriesRepository seriesRepository;
    private final TagRepository tagRepository;
    private final PostTagRepository postTagRepository;
    private final FileStore fileStore;

    public List<String> getWrite(User sessionUser) {
        // 사용자 정보를 조회
        Optional<User> byId = userRepository.findById(getSessionUser(sessionUser).getId());
        User user = byId.orElseThrow(() -> new EntityNotFoundException("User not found"));

        List<Series> seriesList = user.getSeriesList();
        List<String> returnSeriesList = new ArrayList<>();
        // 구성
        for (Series series : seriesList) {
            returnSeriesList.add(series.getSeriesName());
        }

        return returnSeriesList;
    }

    private User getSessionUser(User sessionUser) {
        Optional<User> findUser = userRepository.findById(sessionUser.getId());
        return findUser.orElse(null);
    }

    @Transactional
    public void saveWrite(WriteForm form, User sessionUser, boolean isTemp) throws IOException {

        // 사용자 정보를 조회
        Optional<User> byId = userRepository.findById(getSessionUser(sessionUser).getId());
        User user = byId.orElseThrow(() -> new EntityNotFoundException("User not found"));

        FileMetadata updateFile = null;

        if (form.getThumbnail() != null) {
            updateFile = fileStore.storeFile(form.getThumbnail());
        }

        Series series = seriesRepository.findBySeriesName(form.getSeries()).orElse(null);

        urlProvider(form); // url 설정

        String encodedContent = URLDecoder.decode(form.getContent(), StandardCharsets.UTF_8);

        Post post = Post.builder()
                .title(form.getTitle())
                .content(encodedContent)
                .writable(!form.isOpen())
                .postSummary(form.getPostSummary())
                .postUrl(form.getUrl())
                .series(series)
                .thumbnail(updateFile != null ? updateFile.getStoredFileName() : "/img/default_post.png")
                .user(user)
                .build();

        List<Tag> tags = tagProvider(form.getTags());

        Post postSaved = postRepository.save(post);
        List<Tag> tagsSaved = tagRepository.saveAll(tags);

        List<PostTag> postTagsForBulkSave = new ArrayList<>();

        for (Tag tag : tagsSaved) {
            PostTag postTag = PostTag.builder()
                    .post(postSaved)
                    .tag(tag)
                    .build();

            postTagsForBulkSave.add(postTag);
        }

        postTagRepository.saveAll(postTagsForBulkSave);

    }

    private List<Tag> tagProvider(String tags) {
        if (tags == null || tags.trim().isEmpty()) {
            return List.of();
        }
        List<String> tagsPrev = Arrays.stream(tags.split(","))
                .map(String::trim) // 각 태그의 앞뒤 공백 제거
                .filter(tag -> !tag.isEmpty()) // 빈 태그 제거
                .toList();

        List<Tag> returnTagList = new ArrayList<>();

        for (String tagString : tagsPrev) {
            returnTagList.add(Tag.builder()
                    .name(tagString)
                    .build());
        }

        return returnTagList;
    }

    private void urlProvider(WriteForm form) {
        if (form.getUrl() == null || form.getUrl().isEmpty()) {
            // UUID를 생성하고 제목과 결합
            String uniqueUrl = form.getTitle() + "-" + UUID.randomUUID();
            form.setUrl(uniqueUrl);
        }
    }
}
