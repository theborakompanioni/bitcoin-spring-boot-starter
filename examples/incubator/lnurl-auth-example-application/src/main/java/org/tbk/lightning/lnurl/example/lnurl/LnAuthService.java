package org.tbk.lightning.lnurl.example.lnurl;

import org.tbk.lnurl.LnUrlAuth;

import java.net.URI;

public interface LnAuthService {

    LnUrlAuth createLnUrlAuth();

    boolean verifyLogin(URI loginUri);
}
