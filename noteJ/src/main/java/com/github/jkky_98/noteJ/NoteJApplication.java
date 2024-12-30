package com.github.jkky_98.noteJ;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.core.env.Environment;

@SpringBootApplication
@EnableJpaAuditing
@Slf4j
public class NoteJApplication {

	public static void main(String[] args) {
		SpringApplication.run(NoteJApplication.class, args);
	}

	@Bean
	CommandLineRunner logEnvironmentSettings(Environment environment,
											 @Value("${spring.profiles.active}") String activeProfile,
											 @Value("${server.env}") String serverEnv,
											 @Value("${cloud.aws.credentials.access-key}") String awsAccessKey,
											 @Value("${cloud.aws.credentials.secret-key}") String awsSecretKey,
											 @Value("${cloud.aws.region.static}") String awsRegion,
											 @Value("${spring.datasource.url}") String datasourceUrl) {
		return args -> {
			log.info("========== ENVIRONMENT SETTINGS ==========");
			log.info("Active Profile: {}", activeProfile);
			log.info("Server Env: {}", serverEnv);
			log.info("AWS Access Key: {}", maskSensitiveInfo(awsAccessKey));
			log.info("AWS Secret Key: {}", maskSensitiveInfo(awsSecretKey));
			log.info("AWS Region: {}", awsRegion);
			log.info("Datasource URL: {}", datasourceUrl);
			log.info("==========================================");
		};
	}

	private String maskSensitiveInfo(String value) {
		if (value == null || value.length() < 4) {
			return "****";
		}
		return value.substring(0, 2) + "****" + value.substring(value.length() - 2);
	}
}