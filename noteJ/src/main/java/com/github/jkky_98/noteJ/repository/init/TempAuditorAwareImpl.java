package com.github.jkky_98.noteJ.repository.init;

import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Profile("temp")
public class TempAuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        // 시스템 사용자를 반환
        return Optional.of("system");
    }
}
