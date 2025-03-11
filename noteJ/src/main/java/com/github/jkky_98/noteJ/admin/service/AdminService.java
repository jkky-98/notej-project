package com.github.jkky_98.noteJ.admin.service;

import com.github.jkky_98.noteJ.admin.dto.*;
import com.github.jkky_98.noteJ.admin.mapper.AdminMapper;
import com.github.jkky_98.noteJ.admin.repository.AdminRepository;
import com.github.jkky_98.noteJ.domain.Contact;
import com.github.jkky_98.noteJ.domain.Post;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.repository.PostRepository;
import com.github.jkky_98.noteJ.service.PostService;
import com.github.jkky_98.noteJ.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final AdminRepository adminRepository;
    private final PostRepository postRepository;
    private final AdminMapper adminMapper;
    private final PostService postService;
    private final UserService userService;

    @Transactional(readOnly = true)
    public Page<AdminUserForm> getUsers(AdminUsersCond cond, Pageable pageable) {
        log.info("getUsers cond Email:{}", cond.getEmail());
        Page<User> users = adminRepository.searchUsersAdmin(cond, pageable);
        return adminMapper.userPageToAdminUserFormPage(users);
    }

    @Transactional(readOnly = true)
    public Page<AdminContentsForm> getContents(AdminContentsCond cond, Pageable pageable) {
        Page<Post> posts = adminRepository.searchPostsAdmin(cond, pageable);
        return adminMapper.postPageToAdminContentsFormPage(posts);
    }

    @Transactional(readOnly = true)
    public Page<AdminContactForm> getContacts(Pageable pageable) {
        Page<Contact> contacts = adminRepository.searchContactsAdmin(pageable);
        return adminMapper.contactPageToAdminContactFormPage(contacts);
    }

    @Transactional
    public Long deleteContent(Long postId) {
        Post postRemoved = postService.findById(postId);
        postRepository.deleteById(postRemoved.getId());
        return postRemoved.getId();
    }

    public AdminHomeForm getAdminHomeData() {

        // transaction start
        List<Post> allPosts = postService.findAll();
        List<User> allUsers = userService.findAllUsers();
        // transaction end

        // user수
        long usersSize = allUsers.size();

        // post수
        long postsSize = allPosts.size();

        // post의 모든 viewCount의 합
        long totalViewCount = allPosts
                .stream()
                .mapToLong(Post::getViewCount)
                .sum();

        return new AdminHomeForm(usersSize, postsSize, totalViewCount);
    }
}
