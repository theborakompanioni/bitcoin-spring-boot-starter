package org.tbk.lightning.lnurl.example.security;

import fr.acinq.secp256k1.Hex;
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

import java.util.List;

@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {

    @NonNull
    private final WalletUserService walletUserService;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        WalletUser walletUser = walletUserService.findUser(Hex.decode(username))
                .orElseThrow(() -> new UsernameNotFoundException(username));

        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("WALLET_USER"));
        return User.builder()
                .username(walletUser.getName())
                .password("") // there is no "password" with lnurl-auth - set to arbitrary value
                .authorities(authorities)
                .build();
    }
}
