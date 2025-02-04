package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.domain.user.UserDesc;
import com.github.jkky_98.noteJ.repository.UserRepository;
import com.github.jkky_98.noteJ.web.controller.form.UserViewForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GlobalService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "sessionUserInfoCache", key = "#userId", unless = "#result == null")
    public UserViewForm getNavigationWithSessionUser(Long userId) {

        User fullyInitializedUser = userRepository.findById(userId).orElse(null);
        if (fullyInitializedUser == null) {
            return null;
        }

        UserViewForm userViewForm = getUserViewForm(fullyInitializedUser);
        return userViewForm;
    }

    private static UserViewForm getUserViewForm(User fullyInitializedUser) {
        UserDesc userDesc = fullyInitializedUser.getUserDesc(); // UserDesc 가져오기
        UserViewForm userViewForm = new UserViewForm();

        // 필드 매핑
        userViewForm.setUsername(fullyInitializedUser.getUsername());
        userViewForm.setEmail(fullyInitializedUser.getEmail());
        userViewForm.setProfilePic(userDesc != null && userDesc.getProfilePic() != null
                ? userDesc.getProfilePic()
                : null); // 기본 프로필 이미지
        userViewForm.setUserDesc(userDesc != null && userDesc.getDescription() != null
                ? userDesc.getDescription()
                : "No description available"); // 기본 설명
        userViewForm.setBlogTitle(userDesc != null && userDesc.getBlogTitle() != null
                ? userDesc.getBlogTitle()
                : "Untitled Blog"); // 기본 블로그 제목

        return userViewForm;
    }
}
