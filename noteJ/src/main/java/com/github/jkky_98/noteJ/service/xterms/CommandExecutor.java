package com.github.jkky_98.noteJ.service.xterms;

import com.github.jkky_98.noteJ.domain.user.User;

@FunctionalInterface
public interface CommandExecutor {
    String execute(String command, Long userId);
}
