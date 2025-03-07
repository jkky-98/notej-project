package com.github.jkky_98.noteJ.admin.controller;

import com.github.jkky_98.noteJ.admin.dto.AdminContentsCond;
import com.github.jkky_98.noteJ.admin.dto.AdminContentsDeleteRequest;
import com.github.jkky_98.noteJ.admin.dto.AdminContentsDeleteResponse;
import com.github.jkky_98.noteJ.admin.dto.AdminContentsForm;
import com.github.jkky_98.noteJ.admin.service.AdminService;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.domain.user.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/admin")
    public String getAdmin(
            @SessionAttribute("loginUser") User sessionUser,
            Model model
            ) {
        if (sessionUser != null && sessionUser.getUserRole() == UserRole.ADMIN) {
            model.addAttribute("form", adminService.getAdminHomeData());
            return "admin/admin";
        }
        return "redirect:/";
    }

    @GetMapping("/admin/contents")
    public String getAdminContents(
            @SessionAttribute("loginUser") User sessionUser,
            @ModelAttribute AdminContentsCond cond,
            @PageableDefault(page = 0, size = 10) Pageable pageable,
            Model model
    ) {
        if (sessionUser != null && sessionUser.getUserRole() == UserRole.ADMIN) {
            Page<AdminContentsForm> responseDto = adminService.getContents(cond, pageable);

            model.addAttribute("contents", responseDto);
            return "admin/adminContents";
        }
        return "redirect:/";
    }

    @DeleteMapping("/admin/contents/delete")
    @ResponseBody
    public ResponseEntity<?> deleteAdminContents(
        @RequestBody AdminContentsDeleteRequest dto
    ) {
        try {
            Long postIdSuccessRemove = adminService.deleteContent(dto.getPostId());
            AdminContentsDeleteResponse response = new AdminContentsDeleteResponse();
            response.setPostId(postIdSuccessRemove);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/admin/users")
    public String getAdminUsers(
            @SessionAttribute("loginUser") User sessionUser
    ) {
        if (sessionUser != null && sessionUser.getUserRole() == UserRole.ADMIN) {
            return "admin/adminUsers";
        }
        return "redirect:/";
    }

    @GetMapping("/admin/comments")
    public String getAdminComments(
            @SessionAttribute("loginUser") User sessionUser
    ) {
        if (sessionUser != null && sessionUser.getUserRole() == UserRole.ADMIN) {
            return "admin/adminComments";
        }
        return "redirect:/";
    }

    @GetMapping("/admin/contacts")
    public String getAdminContacts(
            @SessionAttribute("loginUser") User sessionUser,
            @PageableDefault(page = 0, size = 10) Pageable pageable,
            Model model
    ) {
        if (sessionUser != null && sessionUser.getUserRole() == UserRole.ADMIN) {
            model.addAttribute("contacts", adminService.getContacts(pageable));
            return "admin/adminContacts";
        }
        return "redirect:/";
    }
}
