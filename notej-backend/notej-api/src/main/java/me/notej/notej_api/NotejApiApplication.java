package me.notej.notej_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class NotejApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotejApiApplication.class, args);
	}

}
