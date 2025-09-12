package me.notej.notej_api.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000")  // ✅ 허용할 Origin
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true);  // ✅ 쿠키 포함
    }
}
