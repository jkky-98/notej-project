package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.domain.user.UserDesc;
import com.github.jkky_98.noteJ.repository.UserRepository;
import com.github.jkky_98.noteJ.web.controller.dto.SettingDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SettingService {

    private final UserRepository userRepository;

    @Transactional
    public SettingDto getUserSettingData(HttpSession session) {
        SettingDto settingDto = new SettingDto();
        // 세션에서 "loginUser"로 저장된 User 객체를 가져옵니다.
        User sessionUser = (User) session.getAttribute("loginUser");

        if (sessionUser != null) {
            // userRepository를 통해 사용자의 정보를 조회합니다.
            Optional<User> userOptional = userRepository.findById(sessionUser.getId());

            if (userOptional.isPresent()) {
                User user = userOptional.get();
                UserDesc userDesc = user.getUserDesc();

                // SettingForm에 사용자 데이터를 담습니다.
                settingDto.setCommentAlarm(userDesc.isCommentAlarm());
                settingDto.setNoteJAlarm(userDesc.isNoteJAlarm());
                settingDto.setProfilePic(userDesc.getProfilePic() != null ? userDesc.getProfilePic() : "/img/default-profile.png");
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
