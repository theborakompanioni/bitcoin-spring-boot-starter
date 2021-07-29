package org.tbk.lightning.lnurl.example.lnurl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import lombok.extern.slf4j.Slf4j;
import org.tbk.lnurl.K1;

import java.time.Duration;

@Slf4j
public class InMemoryK1Cache implements K1Cache {
    private static final RemovalListener<K1, K1> LOG_REMOVAL_LISTENER = new RemovalListener<>() {
        @Override
        public void onRemoval(RemovalNotification<K1, K1> notification) {
            log.debug("Remove k1 '{}' from cache: {}", notification.getKey(), notification.getCause());
        }
    };

    public final Cache<K1, K1> cache = CacheBuilder.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(5))
            .maximumSize(100_000)
            .removalListener(LOG_REMOVAL_LISTENER)
            .build();

    @Override
    public void put(K1 k1) {
        cache.put(k1, k1);
    }

    @Override
    public boolean isPresent(K1 k1) {
        return cache.getIfPresent(k1) != null;
    }

    @Override
    public void remove(K1 k1) {
        cache.invalidate(k1);
    }
}
