package com.github.jkky_98.noteJ;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
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
											 @Value("${spring.datasource.url}") String datasourceUrl,
											 CacheManager cacheManager) {
		return args -> {
			log.info("========== ENVIRONMENT SETTINGS ==========");
			log.info("Active Profile: {}", activeProfile);
			log.info("Server Env: {}", serverEnv);
			log.info("AWS Access Key: {}", maskSensitiveInfo(awsAccessKey));
			log.info("AWS Secret Key: {}", maskSensitiveInfo(awsSecretKey));
			log.info("AWS Region: {}", awsRegion);
			log.info("Datasource URL: {}", datasourceUrl);
			log.info("Cache Manager: {}", cacheManager.getClass().getName());  // 전체 클래스명 출력
			log.info("========== EHCACHE SETTINGS ==========");

			if (cacheManager != null) {
				log.info("Cache Manager Class: {}", cacheManager.getClass().getSimpleName());

				// JCache (JCacheCacheManager)를 사용하는 경우 내부 캐시 매니저 확인
				if (cacheManager instanceof org.springframework.cache.jcache.JCacheCacheManager jCacheCacheManager) {
					javax.cache.CacheManager jcacheManager = jCacheCacheManager.getCacheManager();
					log.info("  - JCache 내부 CacheManager: {}", jcacheManager.getClass().getName());

					// Ehcache 사용 여부 확인
					if (jcacheManager.getClass().getName().contains("ehcache")) {
						log.info("✅ Ehcache가 JCache 내부에서 사용되고 있음!");
					} else {
						log.info("⚠️ Ehcache가 아닌 다른 JCache 기반 캐시 사용 중!");
					}
				}

				// 모든 캐시 목록 출력
				cacheManager.getCacheNames().forEach(cacheName -> {
					Cache cache = cacheManager.getCache(cacheName);
					log.info("  - Cache: {}", cacheName);
				});
			} else {
				log.warn("Ehcache is NOT configured properly!");
			}
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