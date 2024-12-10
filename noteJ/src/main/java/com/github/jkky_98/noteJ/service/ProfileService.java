package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.domain.user.UserDesc;
import com.github.jkky_98.noteJ.repository.UserRepository;
import com.github.jkky_98.noteJ.web.controller.form.ProfileForm;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;

    public ProfileForm getProfile(String username) {

        ProfileForm profileForm = new ProfileForm();

        // 사용자 정보를 조회
        User user = userRepository.findByUsername(username).orElseThrow(() -> new EntityNotFoundException("User not found"));

        UserDesc userDesc = user.getUserDesc();

        setProfileForm(profileForm, user, userDesc);

        return profileForm;

    }

    private static void setProfileForm(ProfileForm profileForm, User user, UserDesc userDesc) {
        profileForm.setUsername(user.getUsername());
        profileForm.setProfilePic(userDesc.getProfilePic());
        profileForm.setDescription(userDesc.getDescription());
        profileForm.setSocialEmail(userDesc.getSocialEmail());
        profileForm.setSocialGitHub(userDesc.getSocialGitHub());
        profileForm.setSocialTwitter(userDesc.getSocialTwitter());
        profileForm.setSocialFacebook(userDesc.getSocialFacebook());
        profileForm.setSocialOther(userDesc.getSocialOther());
        profileForm.setFollowings(user.getFollowingList().size());
        profileForm.setFollowers(user.getFollowerList().size());
    }
}
