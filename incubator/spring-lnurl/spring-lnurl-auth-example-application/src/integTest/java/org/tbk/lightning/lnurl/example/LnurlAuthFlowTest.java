package org.tbk.lightning.lnurl.example;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.UriComponentsBuilder;
import org.tbk.lnurl.auth.LnurlAuth;
import org.tbk.lnurl.auth.SignedLnurlAuth;
import org.tbk.lnurl.simple.SimpleLnurl;
import org.tbk.lnurl.simple.auth.SimpleLnurlAuth;
import org.tbk.lnurl.test.SimpleLnurlWallet;

import java.security.SecureRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(
        webEnvironment = WebEnvironment.RANDOM_PORT,
        classes = LnurlAuthExampleApplication.class
)
@ActiveProfiles("test")
class LnurlAuthFlowTest {
    private static final SecureRandom random = new SecureRandom();

    private static final Pattern sessionIdPattern = Pattern.compile("SESSION=(.*); Path=.*");

    private static SimpleLnurlWallet testWallet;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeAll
    static void setUpAll() {
        byte[] seed = random.generateSeed(256);
        testWallet = SimpleLnurlWallet.fromSeed(seed);
    }

    @Test
    void lnurlAuthLoginSuccess() {
        /*
         * STEP 1: Create a session for web user (containing a newly created 'k1' value)
         *
         * Create a session with k1 value and return a bech32 encoded lnurl-auth string.
         * Only the browser knowing the session ID can log in after a wallet signed the k1 value.
         *
         * This is done in a browser.
         * A login page can, e.g. display a qr-code for wallets to scan.
         */
        RequestEntity<Void> loginRequest = RequestEntity.get(LnurlAuthExampleApplicationSecurityConfig.lnurlAuthLoginPagePath())
                .build();

        ResponseEntity<String> loginResponseEntity = restTemplate.exchange(loginRequest, String.class);

        // e.g. Set-Cookie -> "SESSION=OTY3ZjJmNTYtZjkzZS00YTkyLTkwNDctZjA3NDU0MmI4MmUx; Path=/; HttpOnly; SameSite=Lax"
        String cookieHeaderValue = loginResponseEntity.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertThat("cookie header present", cookieHeaderValue, not(emptyOrNullString()));
        Matcher cookieIdMatcher = sessionIdPattern.matcher(cookieHeaderValue);
        assertThat("cookie id found", cookieIdMatcher.find(), is(true));
        String sessionId = cookieIdMatcher.group(1);
        assertThat("cookie id valid", sessionId, not(emptyOrNullString()));

        String body = loginResponseEntity.getBody();
        Matcher lnurlAuthMatcher = Pattern.compile(".*\"lightning:(lnurl1.*)\".*").matcher(body);

        assertThat("lnurl-auth string found", lnurlAuthMatcher.find(), is(true));
        String lnurlAuthString = lnurlAuthMatcher.group(1);
        assertThat("lnurl-auth string valid", lnurlAuthString, startsWith("lnurl1"));

        LnurlAuth lnurlAuth = SimpleLnurlAuth.parse(SimpleLnurl.fromBech32(lnurlAuthString));

        SignedLnurlAuth signedLnurlAuth = testWallet.authorize(lnurlAuth);

        /*
         * STEP 2: Login with wallet
         *
         * Call url in lnurl-auth string with needed value (k1, sig, key)
         * This step is done by the wallet.
         * Most likely based on a scanned qr code.
         */
        String walletLoginUrl = UriComponentsBuilder.fromUri(signedLnurlAuth.toLnurl().toUri())
                .scheme(null).host(null).port(null).build()
                .toUriString();
        ResponseEntity<Object> walletLoginResponseEntity = restTemplate.getForEntity(walletLoginUrl, Object.class);
        assertThat(walletLoginResponseEntity.getStatusCode(), is(HttpStatus.OK));

        // assert that the user still cannot see any guarded resource
        RequestEntity<Void> authTestRequest1 = RequestEntity.get("/authenticated.html")
                .header(HttpHeaders.COOKIE, "SESSION=" + sessionId)
                .build();
        ResponseEntity<Object> authTestRequest1ResponseEntity = restTemplate.exchange(authTestRequest1, Object.class);
        assertThat("user still cannot see any guarded resource", authTestRequest1ResponseEntity.getStatusCode(), is(HttpStatus.FORBIDDEN));

        /*
         * STEP 3: Migrate the session for web user
         *
         * This is done in a browser.
         * This request will migrate the session and link the web user with the wallet.
         *
         * This can be initiated as needed by your custom implementation (and is not forced upon you).
         * e.g.
         * - telling the user to click the link after successful auth (pro: simple; con: terrible user experience)
         * - polling on the login page (waiting for a redirect) (pro: simple; con: polling, really? [...] )
         * - called after a websocket response (pro: best UX; con: more complex)
         * - etc.
         *
         * This request "prefers" application/json content.
         * That's why it will response with 200 OK (instead of 3xx with 'Location' header).
         */
        RequestEntity<Void> sessionMigrateRequest = RequestEntity.get(LnurlAuthExampleApplicationSecurityConfig.lnurlAuthSessionLoginPath())
                .header(HttpHeaders.COOKIE, "SESSION=" + sessionId)
                .build();
        ResponseEntity<Object> sessionMigrateRequestResponseEntity = restTemplate.exchange(sessionMigrateRequest, Object.class);

        assertThat(sessionMigrateRequestResponseEntity.getStatusCode(), is(HttpStatus.OK));

        // we have enabled "migrate session" in spring security and validate this behavior
        String migratedCookieHeaderValue = sessionMigrateRequestResponseEntity.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertThat("migrated cookie header present", migratedCookieHeaderValue, not(emptyOrNullString()));
        Matcher migratedCookieIdMatcher = sessionIdPattern.matcher(migratedCookieHeaderValue);
        assertThat("migrated cookie id found", migratedCookieIdMatcher.find(), is(true));
        String migratedSessionId = migratedCookieIdMatcher.group(1);
        assertThat("migrated cookie id valid", migratedSessionId, not(emptyOrNullString()));
        assertThat("Session has been migrated", migratedSessionId, is(not(sessionId)));

        /*
         * STEP 4: User is now logged in and can access guarded resources.
         */
        RequestEntity<Void> authTestRequest2 = RequestEntity.get("/authenticated.html")
                .header(HttpHeaders.COOKIE, "SESSION=" + migratedSessionId)
                .build();
        ResponseEntity<String> authTestRequest2ResponseEntity = restTemplate.exchange(authTestRequest2, String.class);
        assertThat("Web user has been authenticated with wallet linking key", authTestRequest2ResponseEntity.getStatusCode(), is(HttpStatus.OK));
    }
}