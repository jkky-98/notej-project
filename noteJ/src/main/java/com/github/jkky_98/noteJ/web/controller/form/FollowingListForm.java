package com.github.jkky_98.noteJ.web.controller.form;

import com.github.jkky_98.noteJ.web.controller.dto.FollowListPostProfileForm;
import com.github.jkky_98.noteJ.web.controller.dto.FollowingListViewForm;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FollowingListForm {
    List<FollowingListViewForm> followings = new ArrayList<>();
    FollowListPostProfileForm profilePostUser;
}
