package com.github.jkky_98.noteJ;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;

@SpringBootTest
@ActiveProfiles("test")
class NoteJApplicationTests {

	@Autowired
	private Environment environment;

	@Autowired
	private DataSource dataSource;

	@Test
	void checkActiveProfile() {
		String[] activeProfiles = environment.getActiveProfiles();
		System.out.println("✅ 현재 활성화된 프로파일: " + String.join(", ", activeProfiles));
		assertThat(activeProfiles).contains("test");
	}

	@Test
	void printDataSourceInfo() {
		System.out.println("✅ 현재 사용 중인 데이터소스 클래스: " + dataSource.getClass().getName());
		try {
			System.out.println("✅ 데이터소스 URL: " + dataSource.getConnection().getMetaData().getURL());
			System.out.println("✅ 데이터소스 사용자명: " + dataSource.getConnection().getMetaData().getUserName());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

