package org.tbk.spring.lnurl.security.test.app;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbk.lnurl.auth.LnurlAuth;
import org.tbk.lnurl.auth.LnurlAuthFactory;
import org.tbk.spring.lnurl.security.LnurlAuthConfigurer;

import javax.servlet.http.HttpSession;
import java.security.Principal;

@Slf4j
@RestController
@RequiredArgsConstructor
public class LnurlAuthTestCtrl {

    @NonNull
    private final LnurlAuthFactory lnurlAuthFactory;

    /**
     * Simple endpoint returning an lnurl-auth string.
     * This method creates a session linking the user to a specific 'k1' value.
     *
     * @return lnurl-auth string to login the user (with an external wallet software)
     */
    @GetMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> initLogin(HttpSession session) {
        LnurlAuth lnurlAuth = lnurlAuthFactory.createLnUrlAuth();

        session.setAttribute(LnurlAuthConfigurer.defaultSessionK1Key(), lnurlAuth.getK1().toHex());

        return ResponseEntity.ok(lnurlAuth.toLnurl().toLnurlString());
    }

    /**
     * Simple endpoint returning the name of the logged in user.
     * This method will throw an exception if the principal is unauthenticated.
     *
     * @return the name of the authenticated user
     */
    @PreAuthorize("hasRole('LNURL_AUTH_TEST_USER')")
    @GetMapping(value = "/authenticated", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> authenticated(Principal principal) {

        if (principal == null) {
            throw new IllegalStateException("Should have been prevented by spring security..");
        }

        return ResponseEntity.ok(principal.getName());
    }
}
