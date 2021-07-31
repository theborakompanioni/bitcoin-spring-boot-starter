package org.tbk.lightning.lnurl.example.domain;

import org.tbk.lightning.lnurl.example.lnurl.security.LnurlAuthenticationToken;
import org.tbk.lnurl.K1;

public interface WalletUserService {
    WalletUser login(byte[] linkingKey, byte[] signature, K1 k1);
    WalletUser getOrCreateUser(byte[] linkingKey);

    WalletUser login(LnurlAuthenticationToken auth);
}
