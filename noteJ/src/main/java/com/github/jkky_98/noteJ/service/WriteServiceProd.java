package com.github.jkky_98.noteJ.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectTaggingRequest;
import com.amazonaws.services.s3.model.GetObjectTaggingResult;
import com.amazonaws.services.s3.model.ObjectTagging;
import com.amazonaws.services.s3.model.SetObjectTaggingRequest;
import com.github.jkky_98.noteJ.domain.*;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.file.FileStore;
import com.github.jkky_98.noteJ.repository.PostFileRepository;
import com.github.jkky_98.noteJ.repository.PostRepository;
import com.github.jkky_98.noteJ.repository.PostTagRepository;
import com.github.jkky_98.noteJ.repository.TagRepository;
import com.github.jkky_98.noteJ.web.controller.dto.AutoEditPostRequest;
import com.github.jkky_98.noteJ.web.controller.dto.AutoEditPostResponse;
import com.github.jkky_98.noteJ.web.controller.dto.AutoSavePostRequest;
import com.github.jkky_98.noteJ.web.controller.dto.AutoSavePostResponse;
import com.github.jkky_98.noteJ.web.controller.form.WriteForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Profile("!local")
public class WriteServiceProd implements WriteService{

    private final PostRepository postRepository;
    private final SeriesService seriesService;
    private final TagRepository tagRepository;
    private final PostTagRepository postTagRepository;
    private final FileStore fileStore;
    private final UserService userService;
    private final PostService postService;
    private final AmazonS3 amazonS3;
    private final PostFileRepository postFileRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String s3BucketName;

    private static final String DEFAULT_POST_PIC =  "default/thumb.webp";

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

    @Transactional
    public void saveWrite(WriteForm form, Long sessionUserId) throws IOException {

        User userById = userService.findUserById(sessionUserId);

        String storedFileName = handleThumnail(form);
        encodingUrlAndContent(form);

        Post post = Post.of(form, userById, seriesService.getSeries(form.getSeries()), storedFileName);
        Post postSaved = postRepository.save(post);
        setTagsForPost(tagProvider(form.getTags()), postSaved);

        // content에서 영구화 시킬 url List 추출
        List<String> imageFilenames = extractImageFilenames(form.getContent());
        log.info("[content에서 사진 filename 추출] : {}", imageFilenames);
        // S3 파일 태그 제거상태 -> 영구상태
        imageFilenames.forEach(filename -> updateTagToPermanent(filename, s3BucketName));

        // PostFile 추가
        List<PostFile> postFiles = imageFilenames.stream()
                .map(urlImage -> PostFile.of(postSaved, urlImage))
                .toList();

        postFileRepository.saveAll(postFiles);

    }

    @Transactional
    public void saveEditWrite(WriteForm form, String postUrl) throws IOException {

        Post post = postService.findByPostUrl(postUrl);
        post.updateSeries(seriesService.getSeries(form.getSeries()));

        // url, content 인코딩
        encodingUrlAndContent(form);

        // thumnail, series 빼고 업데이트
        post.updatePostWithoutThumbnailAndSeries(form);

        // 기존 Post-PostTag 관계 제거
        post.getPostTags().forEach(postTag -> postTag.getTag().getPostTags().remove(postTag));
        post.getPostTags().clear(); // Post와의 관계 끊기

        editThumnail(form, post);
        setTagsForPost(tagProvider(form.getTags()), post);

        updateTagAndRepository(form, post);
    }

    @Transactional
    public AutoSavePostResponse autoSavePost(AutoSavePostRequest request, Long sessionUserId) {
        User sessionUser = userService.findUserById(sessionUserId);
        Post postTemp = Post.ofSavePostTemp(request, sessionUser);
        postRepository.save(postTemp);

        setTagsForPost(tagProvider(request.getTags()), postTemp);

        AutoSavePostResponse autoSavePostResponse = new AutoSavePostResponse();
        autoSavePostResponse.setPostUrl(postTemp.getPostUrl());
        autoSavePostResponse.setUsername(sessionUser.getUsername());
        return autoSavePostResponse;
    }

    @Transactional
    public AutoEditPostResponse autoEditPost(AutoEditPostRequest request) {

        Optional<Post> byPostUrl = postRepository.findByPostUrl(decodingContent(request.getPostUrl()));

        byPostUrl.ifPresent(post -> {
            if (post.getWritable()) {
                return;
            }
            post.updateEditPostTemp(request);
        });

        AutoEditPostResponse autoEditPostResponse = new AutoEditPostResponse();
        autoEditPostResponse.setPostUrl(request.getPostUrl());
        return autoEditPostResponse;
    }

    private void updateTagAndRepository(WriteForm form, Post post) {
        List<String> imageFilenames = extractImageFilenames(form.getContent());
        List<String> postFilesFromPost = post.getPostFiles().stream()
                .map(postFile -> postFile.getUrl())
                .toList();

        updateTagWriteEdit(imageFilenames, postFilesFromPost);
        updateRepositoryWriteEdit(post, imageFilenames);
    }

    private void updateRepositoryWriteEdit(Post post, List<String> imageFilenames) {
        postFileRepository.deleteByPostId(post.getId());
        postFileRepository.saveAll(imageFilenames.stream()
                .map(urlImage -> PostFile.of(post, urlImage))
                .toList());
    }

    private void updateTagWriteEdit(List<String> imageFilenames, List<String> postFiles) {
        // 추가된 사진파일 s3 태그로 영속화
        imageFilenames.stream()
                        .filter(filename -> !postFiles.contains(filename))
                        .forEach(filename -> updateTagToPermanent(filename, s3BucketName));
        // 사라진 사진파일 s3 제거 태그로 변환
        postFiles.stream()
                        .filter(postFile -> !imageFilenames.contains(postFile))
                        .forEach(postFile -> updateTagToDelete(postFile, s3BucketName));
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
    private void setTagsForPost(List<Tag> tags, Post postSaved) {
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
    private List<Tag> tagProvider(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }

        return tags.stream()
                .map(Tag::of)
                .toList();
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

    private static List<String> extractImageFilenames(String content) {
        // 정규식 패턴 (앞에 ![image alt attribute] 포함, 경로 수정)
        String regex = "!\\[image alt attribute\\]\\(/editor/editor-image-print\\?filename=([a-zA-Z0-9._-]+)\\)";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);

        // 결과를 저장할 리스트
        List<String> imageFilenames = new ArrayList<>();

        // 정규식 매칭
        while (matcher.find()) {
            // 첫 번째 그룹에서 파일명 추출
            String filename = matcher.group(1);
            imageFilenames.add(filename);
        }

        return imageFilenames;
    }

    private void updateTag(String fileName, String s3BucketName, String oldStatus, String newStatus) {
        try {
            // 현재 태그 정보 가져오기
            GetObjectTaggingRequest getTaggingRequest = new GetObjectTaggingRequest(s3BucketName, fileName);
            GetObjectTaggingResult taggingResult = amazonS3.getObjectTagging(getTaggingRequest);
            List<com.amazonaws.services.s3.model.Tag> existingTags = taggingResult.getTagSet();

            // 태그 수정
            List<com.amazonaws.services.s3.model.Tag> updatedTags = new ArrayList<>();
            boolean statusTagUpdated = false;

            for (com.amazonaws.services.s3.model.Tag tag : existingTags) {
                if ("Status".equals(tag.getKey()) && oldStatus.equals(tag.getValue())) {
                    updatedTags.add(new com.amazonaws.services.s3.model.Tag("Status", newStatus));
                    statusTagUpdated = true;
                } else {
                    updatedTags.add(tag);
                }
            }

            // 만약 "Status" 태그가 없었다면, 새 태그 추가
            if (!statusTagUpdated) {
                updatedTags.add(new com.amazonaws.services.s3.model.Tag("Status", newStatus));
            }

            // 업데이트된 태그 설정
            SetObjectTaggingRequest setTaggingRequest = new SetObjectTaggingRequest(
                    s3BucketName,
                    fileName,
                    new ObjectTagging(updatedTags)
            );
            amazonS3.setObjectTagging(setTaggingRequest);

            log.info("S3 파일 태그 업데이트 완료: {} -> 태그 '{}'를 '{}'로 변경", fileName, oldStatus, newStatus);
        } catch (Exception e) {
            log.error("S3 파일 태그 업데이트 실패: {}", e.getMessage());
            throw new RuntimeException("S3 파일 태그 업데이트 실패", e);
        }
    }

    private void updateTagToPermanent(String fileName, String s3BucketName) {
        updateTag(fileName, s3BucketName, "delete", "permanent");
    }

    private void updateTagToDelete(String fileName, String s3BucketName) {
        updateTag(fileName, s3BucketName, "permanent", "delete");
    }

    private static String decodingContent(String content) {
        return URLDecoder.decode(content, StandardCharsets.UTF_8);
    }
}
