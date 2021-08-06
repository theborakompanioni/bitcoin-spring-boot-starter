package org.tbk.lnurl.test;

import org.tbk.lnurl.auth.LinkingKey;
import org.tbk.lnurl.auth.LnurlAuth;
import org.tbk.lnurl.auth.SignedLnurlAuth;

import java.net.URI;

public interface LnurlWallet {

    SignedLnurlAuth authorize(LnurlAuth lnurlAuth);

    LinkingKey deriveLinkingPublicKey(URI uri);
}
