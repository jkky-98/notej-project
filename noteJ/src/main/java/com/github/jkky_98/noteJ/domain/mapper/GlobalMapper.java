package com.github.jkky_98.noteJ.domain.mapper;

import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.web.controller.form.UserNavigationViewForm;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface GlobalMapper {

    @Mapping(target = "username", source = "username")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "profilePic", expression = "java(user.getUserDesc() != null && user.getUserDesc().getProfilePic() != null ? user.getUserDesc().getProfilePic() : null)")
    @Mapping(target = "userDesc", expression = "java(user.getUserDesc() != null && user.getUserDesc().getDescription() != null ? user.getUserDesc().getDescription() : \"No description available\")")
    @Mapping(target = "blogTitle", expression = "java(user.getUserDesc() != null && user.getUserDesc().getBlogTitle() != null ? user.getUserDesc().getBlogTitle() : \"Untitled Blog\")")
    UserNavigationViewForm toUserNavigationViewForm(User user);
}