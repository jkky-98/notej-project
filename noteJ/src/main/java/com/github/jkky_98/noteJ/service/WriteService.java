package com.github.jkky_98.noteJ.service;

import com.github.jkky_98.noteJ.web.controller.form.WriteForm;

import java.io.IOException;

public interface WriteService {
    void getWrite(WriteForm form, Long sessionUserId);
    WriteForm getWriteEdit(Long sessionUserId, String postUrl);
    void saveWrite(WriteForm form, Long sessionUserId) throws IOException;
    void saveEditWrite(WriteForm form, String postUrl) throws IOException;

}
