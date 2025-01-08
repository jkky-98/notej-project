package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.*;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.file.FileStore;
import com.github.jkky_98.noteJ.repository.*;
import com.github.jkky_98.noteJ.web.controller.form.WriteForm;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@RequiredArgsConstructor
public class WriteService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final SeriesRepository seriesRepository;
    private final TagRepository tagRepository;
    private final PostTagRepository postTagRepository;
    private final FileStore fileStore;

    public List<String> getSeriesWithUser(User sessionUser) {
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

    public WriteForm getWriteEdit(User sessionUser, String postTitle) {
        // sessionUser <-> title 검증
        User user = userRepository.findById(sessionUser.getId()).orElseThrow(() -> new EntityNotFoundException("User 엔티티 호출 실패"));

        WriteForm writeForm = new WriteForm();
        Post postEdit = postRepository.findByUserUsernameAndPostUrl(user.getUsername(), postTitle).orElseThrow(() -> new EntityNotFoundException("Post 엔티티 호출 실패"));

        // tag 가져오기
        writeForm.setTitle(postEdit.getTitle());
        writeForm.setTags(getTagsStringforForm(postEdit));
        writeForm.setContent(postEdit.getContent());
        writeForm.setPostSummary(postEdit.getPostSummary());
        writeForm.setOpen(postEdit.getWritable());
        writeForm.setUrl(postEdit.getPostUrl());
        writeForm.setSeries(postEdit.getSeries().getSeriesName());

        return writeForm;
    }

    private static String getTagsStringforForm(Post postEdit) {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (PostTag postTag : postEdit.getPostTags()) {
            String tagName = postTag.getTag().getName();
            if (count != 0) {
                sb.append(",");
            }
            count++;
            sb.append(tagName);
        }

        return sb.toString();
    }

    @Transactional
    public void saveWrite(WriteForm form, User sessionUser, boolean isTemp) throws IOException {

        // 사용자 정보를 조회
        Optional<User> byId = userRepository.findById(getSessionUser(sessionUser).getId());
        User user = byId.orElseThrow(() -> new EntityNotFoundException("User not found"));

        String storedFileName = null;
        if (form.getThumbnail() != null) {
            storedFileName = fileStore.storeFile(form.getThumbnail());
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
                .thumbnail(storedFileName != null ? storedFileName : null)
                .user(user)
                .build();

        List<Tag> tags = tagProvider(form.getTags());

        Post postSaved = postRepository.save(post);
        setTag(tags, postSaved);

    }

    private void setTag(List<Tag> tags, Post postSaved) {
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

    @Transactional
    public void saveEditWrite(WriteForm form, String title) throws IOException {
        Post post = postRepository.findByTitle(title).orElseThrow(() -> new EntityNotFoundException("Post 엔티티 존재하지 않음"));
        post.updatePostWithoutThumbnailAndSeries(form);

        String storedFileName = null;
        if (form.getThumbnail() != null) {
            storedFileName = fileStore.storeFile(form.getThumbnail());
        }

        Series series = seriesRepository.findBySeriesName(form.getSeries()).orElseThrow(() -> new EntityNotFoundException("Series 엔티티 존재하지 않음"));
        post.updateSeries(series);

        // 기존 Post-PostTag 관계 제거
        for (PostTag postTag : post.getPostTags()) {
            postTag.getTag().getPostTags().remove(postTag); // Tag와의 관계 끊기
        }
        post.getPostTags().clear(); // Post와의 관계 끊기

        String thumbnailDeleted = post.getThumbnail();
        fileStore.storeFile(form.getThumbnail()); // 새로운 파일 업로드
        post.updateThumbnail(storedFileName);
        fileStore.deleteFile(thumbnailDeleted); // 기존 파일 삭제

        //Tag 업데이트
        List<Tag> tags = tagProvider(form.getTags());
        setTag(tags, post);

    }


    private User getSessionUser(User sessionUser) {
        Optional<User> findUser = userRepository.findById(sessionUser.getId());
        return findUser.orElse(null);
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
