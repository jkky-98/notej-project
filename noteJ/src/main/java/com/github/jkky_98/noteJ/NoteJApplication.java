package com.github.jkky_98.noteJ;

import com.github.jkky_98.noteJ.domain.base.AuditorAwareImpl;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;
import java.util.UUID;

@SpringBootApplication
@EnableJpaAuditing
public class NoteJApplication {

	public static void main(String[] args) {
		SpringApplication.run(NoteJApplication.class, args);
	}

}
