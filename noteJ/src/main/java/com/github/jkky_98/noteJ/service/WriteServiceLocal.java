package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.*;
import com.github.jkky_98.noteJ.domain.mapper.PostMapper;
import com.github.jkky_98.noteJ.domain.mapper.TagMapper;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.file.FileStore;
import com.github.jkky_98.noteJ.repository.*;
import com.github.jkky_98.noteJ.web.controller.dto.AutoEditPostRequest;
import com.github.jkky_98.noteJ.web.controller.dto.AutoEditPostResponse;
import com.github.jkky_98.noteJ.web.controller.dto.AutoSavePostRequest;
import com.github.jkky_98.noteJ.web.controller.dto.AutoSavePostResponse;
import com.github.jkky_98.noteJ.web.controller.form.WriteForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.github.jkky_98.noteJ.service.util.DefaultConst.DEFAULT_POST_PIC;

@Service
@RequiredArgsConstructor
@Slf4j
@Profile({"local", "test"})
public class WriteServiceLocal implements WriteService {

    private final PostRepository postRepository;
    private final SeriesService seriesService;
    private final TagRepository tagRepository;
    private final PostTagRepository postTagRepository;
    private final FileStore fileStore;
    private final UserService userService;
    private final PostService postService;
    private final TagMapper tagMapper;
    private final PostMapper postMapper;

    /**
     * /write get 요청에 사용될 WriteForm을 구성
     * @param form
     * @param sessionUserId
     */
    @Transactional(readOnly = true)
    public void getWrite(WriteForm form, Long sessionUserId) {

        User userById = userService.findUserById(sessionUserId);

        List<Series> seriesList = userById.getSeriesList();
        // 구성
        List<String> returnSeriesList = seriesList.stream()
                .map(Series::getSeriesName)
                .toList();

        form.setSeriesList(returnSeriesList);
    }

    /**
     * write/{postUrl} get 요청에 사용될 WriteForm을 구성
     * @param sessionUserId
     * @param postUrl
     * @return
     */
    @Transactional(readOnly = true)
    public WriteForm getWriteEdit(Long sessionUserId, String postUrl) {
        User user = userService.findUserById(sessionUserId);

        Post postEdit = postService.findByPostUrl(decodingContent(postUrl));

        return postMapper.toWriteForm(postEdit, user);
    }

    /**
     * /write post 요청에 사용, WriteForm을 사용하여 Post엔티티에 저장
     * @param form
     * @param sessionUserId
     * @throws IOException
     */
    @Transactional
    @CacheEvict(value = "tagCache", key = "#sessionUserId")
    public void saveWrite(WriteForm form, Long sessionUserId) throws IOException {

        User sessionUser = userService.findUserById(sessionUserId);

        String thumbnailPath = handleThumnail(form);

        Series series = seriesService.getSeries(form.getSeries(), sessionUser);

        Post post = postMapper.toPostSaveWrite(
                form,
                sessionUser,
                series,
                thumbnailPath
        );
        // Post save
        Post postSaved = postRepository.save(post);

        // Post에 딸린 Tag 생성 및 저장
        List<Tag> tagList = tagMapper.toTagList(form.getTags());
        setTags(tagList, postSaved);

    }

    /**
     * /write/{postUrl} post 요청에 사용, WriteForm을 사용하여 Post엔티티에 수정
     * @param form
     * @param postUrl
     * @throws IOException
     */
    @Transactional
    @CacheEvict(value = "tagCache", key = "#sessionUser.id")
    public void saveEditWrite(WriteForm form, String postUrl, User sessionUser) throws IOException {

        Post post = postService.findByPostUrl(decodingContent(postUrl));
        post.updateSeries(seriesService.getSeries(form.getSeries(), sessionUser));

        // content 인코딩
        form.setContent(
                decodingContent(form.getContent())
        );

        // thumnail, series 빼고 업데이트
        post.updatePostWithoutThumbnailAndSeries(form);

        // 기존 Post-PostTag 관계 제거
        disconnectPostWithPostTag(post);

        // thumnail 업데이트 로직
        editThumbnail(form, post);

        // Tag 재 설정
        List<Tag> tags = tagMapper.toTagList(form.getTags());
        setTags(tags, post);

    }

    private static void disconnectPostWithPostTag(Post post) {
        post.getPostTags().forEach(postTag -> postTag.getTag().getPostTags().remove(postTag));
        post.getPostTags().clear(); // Post와의 관계 끊기
    }

    @Transactional
    public AutoSavePostResponse autoSavePost(AutoSavePostRequest request, Long sessionUserId) {
        User sessionUser = userService.findUserById(sessionUserId);
        Series series = seriesService.getSeries(request.getSeriesName(), sessionUser);

        Post postTemp = postMapper.toPostForAutoSave(request, sessionUser, series);
        postRepository.save(postTemp);

        List<Tag> tagList = tagMapper.toTagList(request.getTags());
        setTags(tagList, postTemp);

        return postMapper.toAutoSaveResponse(postTemp);
    }

    @Transactional
    public AutoEditPostResponse autoEditPost(AutoEditPostRequest request, Long sessionUserId) {

        Post post = postService.findByPostUrl(decodingContent(request.getPostUrl()));

        if (post.getWritable()) {
            throw new RuntimeException("공개된 post는 AutoEdit될 수 없습니다.");
        }

        User sessionUser = userService.findUserById(sessionUserId);
        Series series = seriesService.getSeries(request.getSeriesName(), sessionUser);
        post.updateEditPostTemp(request, series);

        return postMapper.toAutoEditResponse(post);
    }

    private String handleThumnail(WriteForm form) throws IOException {
        String storedFileName = DEFAULT_POST_PIC;
        if (form.getThumbnail() != null && !form.getThumbnail().isEmpty()) {
            storedFileName = fileStore.storeFile(form.getThumbnail());
        }
        return storedFileName;
    }

    private void editThumbnail(WriteForm form, Post post) throws IOException {
        String newThumbnail = getStoredThumbnail(form);
        String oldThumbnail = post.getThumbnail();

        post.updateThumbnail(newThumbnail);

        if (!oldThumbnail.equals(DEFAULT_POST_PIC)) {
            deleteOldThumbnail(oldThumbnail);
        }
    }

    /**
     * 새 썸네일을 저장하고, 없으면 기본값을 반환하는 메서드
     */
    private String getStoredThumbnail(WriteForm form) throws IOException {
        return (form.getThumbnail() != null && !form.getThumbnail().isEmpty())
                ? fileStore.storeFile(form.getThumbnail())
                : DEFAULT_POST_PIC;
    }

    /**
     * 기존 썸네일이 존재하면 삭제하는 메서드
     */
    private void deleteOldThumbnail(String oldThumbnail) {
        Optional.ofNullable(oldThumbnail)
                .ifPresent(fileStore::deleteFile);
    }

    /**
     * 태그 엔티티들을 영속성 컨텍스트에 저장
     * @param tags
     * @param postSaved
     */

    private void setTags(List<Tag> tags, Post postSaved) {
        List<Tag> tagsSaved = tagRepository.saveAll(tags);
        List<PostTag> postTags = tagsSaved.stream()
                .map(tag -> tagMapper.toPostTag(postSaved, tag))
                .toList();
        postTagRepository.saveAll(postTags);
    }


    private static String decodingContent(String content) {
        return URLDecoder.decode(content, StandardCharsets.UTF_8);
    }

}
