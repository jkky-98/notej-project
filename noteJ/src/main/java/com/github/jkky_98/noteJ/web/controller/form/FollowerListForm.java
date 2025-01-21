package com.github.jkky_98.noteJ.web.controller.form;

import com.github.jkky_98.noteJ.web.controller.dto.FollowListPostProfileDto;
import com.github.jkky_98.noteJ.web.controller.dto.FollowerListViewDto;
import com.github.jkky_98.noteJ.web.controller.dto.FollowingListViewDto;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FollowerListForm {
    List<FollowerListViewDto> followers = new ArrayList<>();
    FollowListPostProfileDto profilePostUser;
}
