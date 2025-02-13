package com.github.jkky_98.noteJ.aop;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ApplicationStartupListener {
    private static boolean isApplicationReady = false;

    @EventListener(org.springframework.boot.context.event.ApplicationReadyEvent.class)
    public void onApplicationReady() {
        isApplicationReady = true;
    }

    public static boolean isReady() {
        return isApplicationReady;
    }
}
