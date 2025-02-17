package com.github.jkky_98.noteJ.service.xterms;

import java.util.List;

@FunctionalInterface
public interface DynamicCommandExecutor {
    String execute(String command, Long userId, List<String> groups);
}
