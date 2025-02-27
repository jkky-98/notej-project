package com.github.jkky_98.noteJ.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectTaggingRequest;
import com.amazonaws.services.s3.model.GetObjectTaggingResult;
import com.amazonaws.services.s3.model.ObjectTagging;
import com.amazonaws.services.s3.model.SetObjectTaggingRequest;
import com.github.jkky_98.noteJ.domain.*;
import com.github.jkky_98.noteJ.domain.mapper.PostFileMapper;
import com.github.jkky_98.noteJ.domain.mapper.PostMapper;
import com.github.jkky_98.noteJ.domain.mapper.TagMapper;
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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.jkky_98.noteJ.service.util.DefaultConst.DEFAULT_POST_PIC;

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
    private final PostMapper postMapper;
    private final TagMapper tagMapper;
    private final PostFileMapper postFileMapper;
    @Value("${cloud.aws.s3.bucket}")
    private String s3BucketName;

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

        Post postEdit = postService.findByPostUrl(postUrl);

        return postMapper.toWriteForm(postEdit, user);
    }

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

        Post postSaved = postRepository.save(post);

        List<Tag> tagList = tagMapper.toTagList(form.getTags());
        setTags(tagList, postSaved);

        updateTagS3Prod(form, postSaved);

    }

    private void updateTagS3Prod(WriteForm form, Post postSaved) {
        // content에서 영구화 시킬 url List 추출
        List<String> imageFilenames = extractImageFilenames(form.getContent());
        // S3 파일 태그 제거상태 -> 영구상태
        imageFilenames.forEach(filename -> updateTagToPermanent(filename, s3BucketName));

        // PostFile 추가
        List<PostFile> postFiles = imageFilenames.stream()
                .map(urlImage -> postFileMapper.toPostFile(postSaved, urlImage))
                .toList();

        postFileRepository.saveAll(postFiles);
    }

    @Transactional
    @CacheEvict(value = "tagCache", key = "#sessionUser.id")
    public void saveEditWrite(WriteForm form, String postUrl, User sessionUser) throws IOException {

        Post post = postService.findByPostUrl(postUrl);
        post.updateSeries(seriesService.getSeries(form.getSeries(), sessionUser));

        // content 인코딩
        form.setContent(
                decodingContent(form.getContent())
        );

        // thumnail, series 빼고 업데이트
        post.updatePostWithoutThumbnailAndSeries(form);

        // 기존 Post-PostTag 관계 제거
        disconnectPostWithPostTag(post);

        // post thumbnail 업데이트
        editThumbnail(form, post);

        // Tag 업데이트
        List<Tag> tags = tagMapper.toTagList(form.getTags());
        setTags(tags, post);

        updateTagAndRepository(form, post);
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
                .map(urlImage -> postFileMapper.toPostFile(post, urlImage))
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

    private String handleThumnail(WriteForm form) throws IOException {
        String storedFileName = DEFAULT_POST_PIC;
        if (form.getThumbnail() != null) {
            storedFileName = fileStore.storeFile(form.getThumbnail());
        }
        return storedFileName;
    }

    private void editThumbnail(WriteForm form, Post post) throws IOException {
        String newThumbnail = getStoredThumbnail(form); // ✅ 새로운 썸네일 저장 또는 기본값 반환
        String oldThumbnail = post.getThumbnail(); // ✅ 기존 썸네일 저장

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
