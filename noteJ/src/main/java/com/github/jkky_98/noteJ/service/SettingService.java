package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.domain.user.UserDesc;
import com.github.jkky_98.noteJ.file.FileStoreLocal;
import com.github.jkky_98.noteJ.repository.UserRepository;
import com.github.jkky_98.noteJ.web.controller.dto.SettingDto;
import com.github.jkky_98.noteJ.web.controller.form.UserSettingsForm;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SettingService {

    private final UserRepository userRepository;
    private final FileStoreLocal fileStoreLocal;

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

        //update
        userDesc.updateSetting(form, fileStoreLocal);

        return Optional.of("success");
    }

    @Transactional
    public SettingDto getUserSettingData(HttpSession session) {
        SettingDto settingDto = new SettingDto();
        // 세션에서 "loginUser"로 저장된 User 객체를 가져옵니다.
        User sessionUser = (User) session.getAttribute("loginUser");

        System.out.println(sessionUser.getUsername());

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
                settingDto.setProfilePic(userDesc.getProfilePic());
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
}
