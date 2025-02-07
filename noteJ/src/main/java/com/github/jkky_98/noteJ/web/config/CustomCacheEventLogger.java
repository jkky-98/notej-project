package com.github.jkky_98.noteJ.web.config;

import lombok.extern.slf4j.Slf4j;
import org.ehcache.event.CacheEvent;
import org.ehcache.event.CacheEventListener;

@Slf4j
public class CustomCacheEventLogger<K, V> implements CacheEventListener<K, V> {

    @Override
    public void onEvent(CacheEvent<? extends K, ? extends V> event) {
        log.info("[CACHE EVENT] Type: {}, Key: {}, Old Value: {}, New Value: {}",
                event.getType(), event.getKey(), event.getOldValue(), event.getNewValue());
    }
}

