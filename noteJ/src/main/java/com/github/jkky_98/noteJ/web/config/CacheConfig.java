package com.github.jkky_98.noteJ.web.config;

import com.github.jkky_98.noteJ.web.controller.form.UserViewForm;
import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.Duration;
import javax.cache.expiry.ModifiedExpiryPolicy;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public JCacheManagerCustomizer cacheManagerCustomizer() {
        return cacheManager -> {
            cacheManager.createCache("sessionUserInfoCache", new MutableConfiguration<Long, UserViewForm>()
                    .setTypes(Long.class, UserViewForm.class)
                    .setStoreByValue(false)  // Store by reference (Ehcache 특성에 맞게)
                    .setExpiryPolicyFactory(ModifiedExpiryPolicy.factoryOf(new Duration(TimeUnit.HOURS, 6))));

            cacheManager.createCache("navigationAlarm", new MutableConfiguration<Long, Long>()
                    .setTypes(Long.class, Long.class)
                    .setStoreByValue(false)
                    .setExpiryPolicyFactory(ModifiedExpiryPolicy.factoryOf(new Duration(TimeUnit.HOURS, 1))));
        };
    }
}

