package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.web.controller.dto.AutoEditPostRequest;
import com.github.jkky_98.noteJ.web.controller.dto.AutoEditPostResponse;
import com.github.jkky_98.noteJ.web.controller.dto.AutoSavePostRequest;
import com.github.jkky_98.noteJ.web.controller.dto.AutoSavePostResponse;
import com.github.jkky_98.noteJ.web.controller.form.WriteForm;

import java.io.IOException;

public interface WriteService {
    void getWrite(WriteForm form, Long sessionUserId);
    WriteForm getWriteEdit(Long sessionUserId, String postUrl);
    void saveWrite(WriteForm form, Long sessionUserId) throws IOException;
    void saveEditWrite(WriteForm form, String postUrl) throws IOException;
    AutoSavePostResponse autoSavePost(AutoSavePostRequest request, Long sessionUserId) throws IOException;
    AutoEditPostResponse autoEditPost(AutoEditPostRequest request) throws IOException;
}
