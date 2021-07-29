package org.tbk.lightning.lnurl.example.domain;

import org.tbk.lnurl.K1;

public interface WalletUserService {
    WalletUser login(byte[] linkingKey, byte[] signature, K1 k1);
}
