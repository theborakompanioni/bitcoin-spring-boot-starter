package org.tbk.lightning.lnurl.example.security;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.tbk.lightning.lnurl.example.domain.WalletUser;
import org.tbk.lightning.lnurl.example.domain.WalletUserService;
import org.tbk.lnurl.simple.auth.SimpleLinkingKey;

import java.time.Instant;
import java.util.List;

@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    @NonNull
    private final WalletUserService walletUserService;

    @Override
    @Transactional
    @SuppressFBWarnings("HARD_CODE_PASSWORD") // okay in this case
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        WalletUser walletUser = walletUserService.findUser(SimpleLinkingKey.fromHex(username))
                .orElseThrow(() -> new UsernameNotFoundException(username));

        Instant now = Instant.now();

        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("WALLET_USER"));
        return User.builder()
                .username(walletUser.getName())
                .password("") // there is no "password" with lnurl-auth - set to arbitrary value
                .authorities(authorities)
                .accountExpired(walletUser.isAccountExpired(now))
                .accountLocked(walletUser.isAccountLocked(now))
                .disabled(!walletUser.isAccountEnabled(now))
                .credentialsExpired(walletUser.isCredentialsExpired(now))
                .build();
    }
}
