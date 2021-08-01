package org.tbk.lnurl.auth;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import lombok.extern.slf4j.Slf4j;
import org.tbk.lnurl.K1;

import java.time.Duration;
import java.util.Optional;

@Slf4j
public final class InMemoryLnurlAuthPairingService implements LnurlAuthPairingService {
    private static final RemovalListener<K1, byte[]> LOG_REMOVAL_LISTENER = new RemovalListener<>() {
        @Override
        public void onRemoval(RemovalNotification<K1, byte[]> notification) {
            log.debug("Remove k1 '{}' from in-memory pairing cache: {}", notification.getKey(), notification.getCause());
        }
    };

    public final Cache<K1, byte[]> cache = CacheBuilder.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(5))
            .maximumSize(100_000)
            .removalListener(LOG_REMOVAL_LISTENER)
            .build();

    @Override
    public Optional<byte[]> findPairedLinkingKeyByK1(K1 k1) {
        return Optional.ofNullable(cache.getIfPresent(k1));
    }

    @Override
    public void pairK1WithLinkingKey(K1 k1, byte[] linkingKey) {
        cache.put(k1, linkingKey);
    }
}
