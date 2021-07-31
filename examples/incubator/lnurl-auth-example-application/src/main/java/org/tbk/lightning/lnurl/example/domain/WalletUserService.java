package org.tbk.lightning.lnurl.example.domain;

import org.tbk.lightning.lnurl.example.lnurl.security.LnurlAuthSessionToken;
import org.tbk.lightning.lnurl.example.lnurl.security.LnurlAuthWalletToken;
import org.tbk.lnurl.K1;

import java.util.Optional;

public interface WalletUserService {

    Optional<WalletUser> findUser(byte[] linkingKey);
    WalletUser findUserCreateIfMissing(byte[] linkingKey);

    // todo: move away so user can extend class for spring-security
    void login(LnurlAuthWalletToken auth);

    Optional<WalletUser> login(LnurlAuthSessionToken auth);
}
