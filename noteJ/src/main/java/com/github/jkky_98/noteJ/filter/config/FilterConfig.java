package com.github.jkky_98.noteJ.filter.config;

import com.github.jkky_98.noteJ.filter.AuthorizationUsernamePasswordFilter;
import com.github.jkky_98.noteJ.filter.RateLimitingFilter;
import jakarta.servlet.Filter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class FilterConfig {

    private final CacheManager cacheManager;

    @Bean
    public FilterRegistrationBean rateLimitingFilter() {
        FilterRegistrationBean<Filter> filterFilterRegistrationBean = new FilterRegistrationBean<>();
        filterFilterRegistrationBean.setFilter(new RateLimitingFilter(cacheManager));
        filterFilterRegistrationBean.setOrder(1);
        filterFilterRegistrationBean.addUrlPatterns("/editor/image-upload");
        return filterFilterRegistrationBean;
    }
    @Bean
    public FilterRegistrationBean authorizationUsernamePasswordFilter() {
        FilterRegistrationBean<Filter> filterFilterRegistrationBean = new FilterRegistrationBean<>();
        filterFilterRegistrationBean.setFilter(new AuthorizationUsernamePasswordFilter());
        filterFilterRegistrationBean.setOrder(2);
        filterFilterRegistrationBean.addUrlPatterns("/*");
        return filterFilterRegistrationBean;
    }
}
