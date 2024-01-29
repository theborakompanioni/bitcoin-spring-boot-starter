package org.tbk.spring.lnurl.security.test.shared;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.UserDetailsManager;
import org.tbk.lnurl.auth.K1;
import org.tbk.lnurl.auth.LinkingKey;
import org.tbk.lnurl.auth.SignedLnurlAuth;
import org.tbk.spring.lnurl.security.userdetails.LnurlAuthUserPairingService;

import java.time.Duration;
import java.util.Optional;

@RequiredArgsConstructor
public class TestPairingService implements LnurlAuthUserPairingService {
    private final UserDetailsManager userDetailsManager;

    private final Cache<K1, LinkingKey> cache = CacheBuilder.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(1))
            .build();

    @Override
    public UserDetails pairUserWithK1(SignedLnurlAuth auth) {
        UserDetails user = createUserIfMissing(auth.getLinkingKey());

        cache.put(auth.getK1(), auth.getLinkingKey());

        return user;
    }

    @Override
    public Optional<UserDetails> findPairedUserByK1(K1 k1) {
        return Optional.ofNullable(cache.getIfPresent(k1))
                .map(it -> userDetailsManager.loadUserByUsername(it.toHex()));
    }

    private UserDetails createUserIfMissing(LinkingKey linkingKey) {
        try {
            return userDetailsManager.loadUserByUsername(linkingKey.toHex());
        } catch (UsernameNotFoundException e) {
            UserDetails user = User.builder()
                    .username(linkingKey.toHex())
                    .password("") // password must not be null -_-
                    .authorities(new SimpleGrantedAuthority("ROLE_LNURL_AUTH_TEST_USER"))
                    .build();
            userDetailsManager.createUser(user);
            return user;
        }
    }
}
