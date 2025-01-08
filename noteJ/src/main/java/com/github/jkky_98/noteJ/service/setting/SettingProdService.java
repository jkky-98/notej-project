package com.github.jkky_98.noteJ.service.setting;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.domain.user.UserDesc;
import com.github.jkky_98.noteJ.file.FileStore;
import com.github.jkky_98.noteJ.repository.UserRepository;
import com.github.jkky_98.noteJ.web.controller.dto.SettingDto;
import com.github.jkky_98.noteJ.web.controller.form.UserSettingsForm;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Profile("!local")
public class SettingProdService implements SettingServiceInterface{

    private final UserRepository userRepository;
    private final FileStore fileStore;

    // 파일 업로드 경로
    @Value("${cloud.aws.s3.bucket}")
    private String s3BucketName;

    private final AmazonS3 amazonS3;

    //toDo: 세션 객체 의존 없애기 컨트롤러에서 처리(어차피 바꿀 인증시스템이므로 의존도를 깊게 내리지 말자)
    @Transactional
    public Optional<String> saveSettings(UserSettingsForm form, User sessionUser) throws IOException {

        // 사용자 정보를 조회
        Optional<User> findSettingUser = userRepository.findById(sessionUser.getId());
        if (!findSettingUser.isPresent()) {
            return Optional.empty();  // 사용자가 DB에 없으면 비어있는 Optional 반환
        }

        // 사용자 정보가 있다면, 해당 사용자의 username 반환
        User user = findSettingUser.get();
        UserDesc userDesc = user.getUserDesc();

        // ProfilePic 저장 FileStore 이용
        String newProfilePicPath = null;

        if (form.getProfilePic() != null) {
            try {
                String profilePicMetadata = fileStore.storeFile(form.getProfilePic());
                newProfilePicPath = profilePicMetadata;
            } catch (IOException e) {
                // 예외 처리: 파일 저장 실패 시 로그 출력 및 적절한 처리
                log.error("Profile picture in Setting upload failed", e);
                // 필요 시, 사용자에게 실패 메시지 반환
            }
        }

        //엔티티 update
        userDesc.updateSetting(form, newProfilePicPath);

        return Optional.of("success");
    }

    @Transactional
    public SettingDto getUserSettingData(User sessionUser) {
        SettingDto settingDto = new SettingDto();

        if (sessionUser != null) {
            // userRepository를 통해 사용자의 정보를 조회합니다.
            Optional<User> userOptional = userRepository.findById(sessionUser.getId());

            if (userOptional.isPresent()) {
                User user = userOptional.get();
                UserDesc userDesc = user.getUserDesc();

                // SettingForm에 사용자 데이터를 담습니다.
                settingDto.setCommentAlarm(userDesc.isCommentAlarm());
                settingDto.setDescription(userDesc.getDescription());
                settingDto.setBlogTitle(userDesc.getBlogTitle());
                settingDto.setNoteJAlarm(userDesc.isNoteJAlarm());
                settingDto.setProfilePic(getProfilePicUrlS3(s3BucketName, userDesc.getProfilePic()));
                settingDto.setTheme(userDesc.getTheme());
                settingDto.setSocialEmail(userDesc.getSocialEmail());
                settingDto.setSocialFacebook(userDesc.getSocialFacebook());
                settingDto.setSocialGitHub(userDesc.getSocialGitHub());
                settingDto.setSocialTwitter(userDesc.getSocialTwitter());
                settingDto.setSocialOther(userDesc.getSocialOther());
                // 추가적으로 설정이 필요한 데이터가 있다면 여기에 추가
                return settingDto;
            }
            return settingDto;
        } else {
            // 세션에 로그인된 사용자 정보가 없을 경우
            throw new IllegalStateException("User is not logged in");
        }

    }

    private String getProfilePicUrlS3(String s3BucketName, String profilePic) {

        return String.format("https://%s.s3.%s.amazonaws.com/%s",
                s3BucketName,
                amazonS3.getRegionName(), // S3 버킷의 리전
                profilePic // S3 오브젝트 키
        );
    }
}
