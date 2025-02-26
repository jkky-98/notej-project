package com.github.jkky_98.noteJ.domain.mapper;

import com.github.jkky_98.noteJ.domain.user.ThemeMode;
import com.github.jkky_98.noteJ.domain.user.User;
import com.github.jkky_98.noteJ.domain.user.UserDesc;
import com.github.jkky_98.noteJ.domain.user.UserRole;
import com.github.jkky_98.noteJ.service.util.DefaultConst;
import com.github.jkky_98.noteJ.web.controller.form.SignUpForm;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import java.time.LocalDateTime;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE,
        imports = {UserRole.class, LocalDateTime.class, ThemeMode.class}
)
public interface UserMapper {

    @Mapping(source = "form.username", target = "username")
    @Mapping(source = "form.email", target = "email")
    @Mapping(source = "form.password", target = "password")
    @Mapping(target = "userRole", expression = "java(UserRole.USER)")
    @Mapping(source = "userDesc", target = "userDesc")
    @Mapping(target = "accountExpiredTime", expression = "java(LocalDateTime.now())")
    User toUserSignUp(SignUpForm form, UserDesc userDesc);

    @Mapping(source = "form.blogTitle", target = "blogTitle")
    @Mapping(source = "form.email", target = "socialEmail")
    @Mapping(target = "commentAlarm", constant = "true")
    @Mapping(target = "noteJAlarm", constant = "true")
    @Mapping(target = "profilePic", constant = DefaultConst.DEFUALT_PROFILE_PIC)
    @Mapping(target = "theme", expression = "java(ThemeMode.LIGHT)")
    @Mapping(target = "description", constant = "")
    UserDesc toUserDescSignUp(SignUpForm form);
}
