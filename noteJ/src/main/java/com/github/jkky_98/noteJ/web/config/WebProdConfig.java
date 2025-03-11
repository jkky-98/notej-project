package com.github.jkky_98.noteJ.web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Profile({"green", "blue"})
public class WebProdConfig implements WebMvcConfigurer {

    @Value("${cloud.aws.s3.bucket}")
    private String s3BucketName;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // /storage/** 경로를 통해 S3 파일에 접근하도록 설정
        registry.addResourceHandler("/storage/**")
                // S3 버킷을 URL로 사용
                .addResourceLocations("https://"+s3BucketName+".s3.amazonaws.com/");
    }

}