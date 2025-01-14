package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.*;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.file.FileStore;
import com.github.jkky_98.noteJ.repository.*;
import com.github.jkky_98.noteJ.web.controller.form.WriteForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WriteService {

    private final PostRepository postRepository;
    private final SeriesService seriesService;
    private final TagRepository tagRepository;
    private final PostTagRepository postTagRepository;
    private final FileStore fileStore;
    private final UserService userService;
    private final PostService postService;

    private static final String DEFAULT_POST_PIC =  "/storage/default/default_post.png";

    /**
     * /write get 요청에 사용될 WriteForm을 구성
     * @param form
     * @param sessionUserId
     */
    @Transactional
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
    @Transactional
    public WriteForm getWriteEdit(Long sessionUserId, String postUrl) {
        User user = userService.findUserById(sessionUserId);

        Post postEdit = postService.findByPostUrl(postUrl);

        return WriteForm.of(postEdit, user);
    }

    /**
     * /write post 요청에 사용, WriteForm을 사용하여 Post엔티티에 저장
     * @param form
     * @param sessionUserId
     * @throws IOException
     */
    @Transactional
    public void saveWrite(WriteForm form, Long sessionUserId) throws IOException {

        // 사용자 정보를 조회
        User userById = userService.findUserById(sessionUserId);

        // 썸네일 설정
        String storedFileName = handleThumnail(form);

        // url, content 인코딩
        encodingUrlAndContent(form);

        // Post Entity 생성
        Post post = Post.of(form, userById, seriesService.getSeries(form.getSeries()), storedFileName);

        // Post save
        Post postSaved = postRepository.save(post);

        // Post에 딸린 Tag 생성 및 저장
        setTag(tagProvider(form.getTags()), postSaved);

    }

    private void encodingUrlAndContent(WriteForm form) {
        urlProvider(form); // url 설정
        encodedContent(form); // content 인코딩
    }

    private String handleThumnail(WriteForm form) throws IOException {
        String storedFileName = DEFAULT_POST_PIC;
        if (form.getThumbnail() != null) {
            storedFileName = fileStore.storeFile(form.getThumbnail());
        }
        return storedFileName;
    }

    /**
     * /write/{postUrl} post 요청에 사용, WriteForm을 사용하여 Post엔티티에 수정
     * @param form
     * @param postUrl
     * @throws IOException
     */
    @Transactional
    public void saveEditWrite(WriteForm form, String postUrl) throws IOException {
        // Post 엔티티 조회
        Post post = postService.findByPostUrl(postUrl);

        // Series 업데이트
        post.updateSeries(seriesService.getSeries(form.getSeries()));

        // url, content 인코딩
        encodingUrlAndContent(form);

        // thumnail, series 빼고 업데이트
        post.updatePostWithoutThumbnailAndSeries(form);

        // 기존 Post-PostTag 관계 제거
        post.getPostTags().forEach(postTag -> postTag.getTag().getPostTags().remove(postTag));
        post.getPostTags().clear(); // Post와의 관계 끊기

        // thumnail 업데이트 로직
        editThumnail(form, post);

        // Tag 업데이트
        setTag(tagProvider(form.getTags()), post);

    }

    private void editThumnail(WriteForm form, Post post) throws IOException {
        if (form.getThumbnail() != null && !form.getThumbnail().isEmpty()) {
            String thumbnailDeleted = post.getThumbnail(); //지울 썸네일 사진파일 이름
            String storedFileName = fileStore.storeFile(form.getThumbnail()); //새로운 파일 업로드

            post.updateThumbnail(storedFileName);
            if (thumbnailDeleted != null) {
                log.info("delete thumbnail : {}", thumbnailDeleted);
                fileStore.deleteFile(thumbnailDeleted); // 기존 파일 삭제
            }
        }
    }

    /**
     * 수정시 태그 엔티티들을 태그 문자열(ex. "java,spring,jpa")로 변환
     * @param postEdit
     * @return
     */
    private static String getTagsStringforForm(Post postEdit) {
        return postEdit.getPostTags().stream()
                .map(postTag -> postTag.getTag().getName())
                .collect(Collectors.joining(","));
    }

    /**
     * 태그 엔티티들을 영속성 컨텍스트에 저장
     * @param tags
     * @param postSaved
     */
    private void setTag(List<Tag> tags, Post postSaved) {
        List<Tag> tagsSaved = tagRepository.saveAll(tags);
        List<PostTag> postTagsForBulkSave = tagsSaved.stream()
                .map(tag -> PostTag.of(postSaved, tag))
                .toList();
        postTagRepository.saveAll(postTagsForBulkSave);
    }

    /**
     * content 인코딩
     * @param form
     */
    private static void encodedContent(WriteForm form) {
        String encodedContent = URLDecoder.decode(form.getContent(), StandardCharsets.UTF_8); // content 설정
        form.setContent(encodedContent);
    }

    /**
     * 태그 문자열를 태그 엔티티들로 변환 (getTagsStringforForm와 counterpart)
     * @param tags
     * @return
     */
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

    /**
     * url 생성기(title + UUID)
     * @param form
     */
    private void urlProvider(WriteForm form) {
        if (form.getUrl() == null || form.getUrl().isEmpty()) {
            // UUID를 생성하고 제목과 결합
            String uniqueUrl = form.getTitle() + "-" + UUID.randomUUID();
            form.setUrl(uniqueUrl);
        }
    }


}
