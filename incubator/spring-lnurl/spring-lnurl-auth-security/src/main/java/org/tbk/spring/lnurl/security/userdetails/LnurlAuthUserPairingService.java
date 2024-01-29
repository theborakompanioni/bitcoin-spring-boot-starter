package org.tbk.spring.lnurl.security.userdetails;

import org.springframework.security.core.userdetails.UserDetails;
import org.tbk.lnurl.auth.K1;
import org.tbk.lnurl.auth.SignedLnurlAuth;

import java.util.Optional;

public interface LnurlAuthUserPairingService {

    /**
     * Pair a user with the <code>k1</code> from {@link SignedLnurlAuth#getK1()}.
     * This method is called during the wallet authentication process and typically
     * invoked from an external device by scanning a QR-Code.
     * <p>
     * Standard use-case is to find a user by its linking key via {@link SignedLnurlAuth#getLinkingKey()}.
     * This is the place to create a user if it does not exist yet.
     * Optionally, base this decision on the action param from {@link SignedLnurlAuth#getAction()}.
     * <p>
     * The given {@link SignedLnurlAuth} object is already validated and verified.
     *
     * @param auth The signed lnurl-auth arguments.
     * @return An existing or newly created user object.
     */
    UserDetails pairUserWithK1(SignedLnurlAuth auth);

    /**
     * Find a user previously paired with {@link LnurlAuthUserPairingService#findPairedUserByK1(K1)}.
     * This method is called during the session authentication process and typically
     * invoked by a browser.
     *
     * @param k1 The <code>k1</code> value used to look up a user.
     * @return An existing user previously paired with the <code>k1</code> value.
     */
    Optional<UserDetails> findPairedUserByK1(K1 k1);
}
