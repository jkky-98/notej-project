package com.github.jkky_98.noteJ.web.controller.global;

import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.domain.user.UserDesc;
import com.github.jkky_98.noteJ.repository.UserRepository;
import com.github.jkky_98.noteJ.web.controller.form.UserViewForm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final UserRepository userRepository;

    @ModelAttribute("currentUrl")
    public String currentUrl(HttpServletRequest request) {
        String queryString = request.getQueryString();
        return request.getRequestURI() + (queryString != null ? "?" + queryString : "");
    }

    @ModelAttribute
    public void addSessionUserToModel(HttpSession session, Model model) {
        // 세션에서 사용자 ID 가져오기
        User sessionUser = (User) session.getAttribute("loginUser");
        if (sessionUser == null) {
            return; // 세션에 사용자 정보가 없으면 바로 반환
        }

        // User 엔티티를 데이터베이스에서 다시 조회하여 초기화된 상태로 가져오기
        User fullyInitializedUser = userRepository.findById(sessionUser.getId())
                .orElse(null);
        if (fullyInitializedUser == null) {
            return; // 데이터베이스에 사용자가 없으면 반환
        }

        // UserViewForm에 데이터 매핑
        UserViewForm userViewForm = getUserViewForm(fullyInitializedUser);

        // 모델에 추가
        model.addAttribute("sessionUser", userViewForm);
    }

    private static UserViewForm getUserViewForm(User fullyInitializedUser) {
        UserDesc userDesc = fullyInitializedUser.getUserDesc(); // UserDesc 가져오기
        UserViewForm userViewForm = new UserViewForm();

        // 필드 매핑
        userViewForm.setUsername(fullyInitializedUser.getUsername());
        userViewForm.setEmail(fullyInitializedUser.getEmail());
        userViewForm.setProfilePic(userDesc != null && userDesc.getProfilePic() != null
                ? userDesc.getProfilePic()
                : "/img/default-profile.png"); // 기본 프로필 이미지
        userViewForm.setUserDesc(userDesc != null && userDesc.getDescription() != null
                ? userDesc.getDescription()
                : "No description available"); // 기본 설명
        userViewForm.setBlogTitle(userDesc != null && userDesc.getBlogTitle() != null
                ? userDesc.getBlogTitle()
                : "Untitled Blog"); // 기본 블로그 제목

        return userViewForm;
    }


}
