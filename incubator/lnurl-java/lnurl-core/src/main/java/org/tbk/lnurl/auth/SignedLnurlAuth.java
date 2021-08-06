package org.tbk.lnurl.auth;

public interface SignedLnurlAuth extends LnurlAuth {
    String LNURL_AUTH_SIG_KEY = "sig";
    String LNURL_AUTH_KEY_KEY = "key";

    Signature getSignature();

    LinkingKey getLinkingKey();
}
