package org.tbk.spring.lnurl.security.test.app;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.provisioning.UserDetailsManager;
import org.tbk.lnurl.auth.K1;
import org.tbk.lnurl.auth.LinkingKey;
import org.tbk.lnurl.auth.LnurlAuthPairingService;

import java.time.Duration;
import java.util.Optional;

@RequiredArgsConstructor
public class TestPairingService implements LnurlAuthPairingService {
    private final UserDetailsManager userDetailsManager;

    private final Cache<K1, LinkingKey> cache = CacheBuilder.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(1))
            .build();

    @Override
    public Optional<LinkingKey> findPairedLinkingKeyByK1(K1 k1) {
        return Optional.ofNullable(cache.getIfPresent(k1));
    }

    @Override
    public boolean pairK1WithLinkingKey(K1 k1, LinkingKey linkingKey) {
        createUserIfMissing(k1, linkingKey);

        cache.put(k1, linkingKey);

        return false;
    }

    private void createUserIfMissing(K1 k1, LinkingKey linkingKey) {
        boolean userExists = userDetailsManager.userExists(linkingKey.toHex());
        if (!userExists) {
            userDetailsManager.createUser(User.builder()
                    .username(linkingKey.toHex())
                    .password(k1.toHex()) // password must not be null -_-
                    .authorities(new SimpleGrantedAuthority("ROLE_LNURL_AUTH_TEST_USER"))
                    .build());
        }
    }
}
