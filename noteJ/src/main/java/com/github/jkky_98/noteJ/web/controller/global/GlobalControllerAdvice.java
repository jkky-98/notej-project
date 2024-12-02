package com.github.jkky_98.noteJ.web.controller.global;

import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.domain.user.UserDesc;
import com.github.jkky_98.noteJ.web.controller.dto.UserViewForm;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @ModelAttribute
    public void addSessionUserToModel(HttpSession session, Model model) {
        User sessionUser = (User) session.getAttribute("loginUser");

        if (sessionUser != null) {
            UserViewForm userViewForm = new UserViewForm();
            UserDesc sessionUserDesc = sessionUser.getUserDesc();

            // 사용자 정보를 UserViewForm으로 매핑
            userViewForm.setUsername(sessionUser.getUsername());
            userViewForm.setEmail(sessionUser.getEmail());
            userViewForm.setProfilePic(
                    sessionUserDesc != null && sessionUserDesc.getProfilePic() != null
                            ? sessionUserDesc.getProfilePic()
                            : "/img/default-profile.png" // 기본 이미지 설정
            );
            userViewForm.setUserDesc(
                    sessionUserDesc != null && sessionUserDesc.getDescription() != null
                            ? sessionUserDesc.getDescription()
                            : "No description available" // 기본 설명 설정
            );

            // 모델에 sessionUser 속성 추가
            model.addAttribute("sessionUser", userViewForm);
        }
    }
}
