package org.tbk.lnurl.test;

import org.tbk.lnurl.auth.LnurlAuth;
import org.tbk.lnurl.auth.SignedLnurlAuth;

public interface LnurlService {

    LnurlAuth createLnUrlAuth();

    boolean verify(SignedLnurlAuth auth);
}
