package com.github.jkky_98.noteJ.web.config;

import com.github.jkky_98.noteJ.web.controller.form.UserViewForm;
import lombok.extern.slf4j.Slf4j;
import org.ehcache.event.EventFiring;
import org.ehcache.event.EventOrdering;
import org.ehcache.event.EventType;
import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.Duration;
import javax.cache.expiry.ModifiedExpiryPolicy;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
@Slf4j
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

            cacheManager.createCache("tagCache", new MutableConfiguration<Long, List>()
                    .setTypes(Long.class, List.class)
                    .setStoreByValue(false)
                    .setExpiryPolicyFactory(ModifiedExpiryPolicy.factoryOf(new Duration(TimeUnit.HOURS, 6))));

            // 모든 캐시에 이벤트 리스너 등록
            registerCacheEventListeners(cacheManager);
        };
    }

    private void registerCacheEventListeners(javax.cache.CacheManager cacheManager) {
        for (String cacheName : cacheManager.getCacheNames()) {
            javax.cache.Cache<?, ?> cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                org.ehcache.Cache<?, ?> ehCache = cache.unwrap(org.ehcache.Cache.class);
                ehCache.getRuntimeConfiguration()
                        .registerCacheEventListener(new CustomCacheEventLogger<>(),
                                EventOrdering.UNORDERED,
                                EventFiring.ASYNCHRONOUS,
                                EnumSet.of(EventType.CREATED, EventType.UPDATED, EventType.EXPIRED, EventType.REMOVED));

                log.info("Registered cache event listener for {}", cacheName);
            }
        }
    }
}
