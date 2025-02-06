package com.github.jkky_98.noteJ.web.controller.form;

import com.github.jkky_98.noteJ.web.controller.dto.FollowListPostProfileForm;
import com.github.jkky_98.noteJ.web.controller.dto.FollowerListViewForm;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FollowerListForm {
    List<FollowerListViewForm> followers = new ArrayList<>();
    FollowListPostProfileForm profilePostUser;
}
