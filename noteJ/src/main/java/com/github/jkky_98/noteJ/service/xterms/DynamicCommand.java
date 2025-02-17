package com.github.jkky_98.noteJ.service.xterms;

import java.util.regex.Pattern;

public class DynamicCommand {
    private final Pattern pattern;
    private final DynamicCommandExecutor executor;

    public DynamicCommand(Pattern pattern, DynamicCommandExecutor executor) {
        this.pattern = pattern;
        this.executor = executor;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public DynamicCommandExecutor getExecutor() {
        return executor;
    }
}