package com.github.jkky_98.noteJ.admin.mapper;

import com.github.jkky_98.noteJ.admin.dto.AdminContactForm;
import com.github.jkky_98.noteJ.admin.dto.AdminContentsForm;
import com.github.jkky_98.noteJ.domain.Contact;
import com.github.jkky_98.noteJ.domain.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface AdminMapper {

    @Mapping(source = "post.id" , target = "postId")
    @Mapping(source = "post.user.username", target = "username")
    @Mapping(source = "post.title", target = "postTitle")
    @Mapping(source = "post", target = "postUrl", qualifiedByName = "generatePostUrl")
    @Mapping(source = "post.createDt", target = "createDt")
    @Mapping(source = "post.viewCount", target = "viewCount")
    @Mapping(target = "likeCount", expression = "java(post.getLikes().size())")
    AdminContentsForm PostToAdminContentsForm(Post post);

    List<AdminContentsForm> postListToAdminContentsFormList(List<Post> posts);

    @Named("generatePostUrl")
    static String generatePostUrl(Post post) {
        if (post == null || post.getPostUrl() == null) {
            return "/";
        }
        return "/@" + post.getUser().getUsername() + "/post/" + post.getPostUrl();
    }

    default Page<AdminContentsForm> postPageToAdminContentsFormPage(Page<Post> postPage) {
        List<AdminContentsForm> contentList = postListToAdminContentsFormList(postPage.getContent());
        return new PageImpl<>(contentList, postPage.getPageable(), postPage.getTotalElements());
    }

    @Mapping(source = "contact.user.username", target = "username")
    AdminContactForm AdminContactFormFromContact(Contact contact);

    List<AdminContactForm> AdminContactFormListToAdminContactFormList(List<Contact> contacts);

    default Page<AdminContactForm> contactPageToAdminContactFormPage(Page<Contact> contactPage) {
        List<AdminContactForm> contactList = AdminContactFormListToAdminContactFormList(contactPage.getContent());
        return new PageImpl<>(contactList, contactPage.getPageable(), contactPage.getTotalElements());
    }
}
