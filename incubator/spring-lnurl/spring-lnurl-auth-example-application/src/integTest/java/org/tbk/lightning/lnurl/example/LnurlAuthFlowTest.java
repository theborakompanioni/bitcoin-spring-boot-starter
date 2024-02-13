package org.tbk.lightning.lnurl.example;

import kotlin.Pair;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
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
import org.tbk.lnurl.test.LnurlWallet;
import org.tbk.lnurl.test.SimpleLnurlWallet;

import java.security.SecureRandom;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import static org.tbk.lightning.lnurl.example.LnurlAuthExampleApplicationSecurityConfig.lnurlAuthLoginPagePath;
import static org.tbk.lightning.lnurl.example.LnurlAuthExampleApplicationSecurityConfig.lnurlAuthSessionLoginPath;

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
    void lnurlAuthLoginSuccessBrowserUser() {
        /*
         * STEP 1: Create a session for web user (containing a newly created 'k1' value)
         *
         * Create a session with k1 value and return a bech32 encoded lnurl-auth string.
         * Only the browser knowing the session ID can log in after a wallet signed the k1 value.
         *
         * This is done in a browser.
         * A login page can, e.g. display a qr-code for wallets to scan.
         */
        RequestEntity<Void> loginRequest = RequestEntity.get(lnurlAuthLoginPagePath())
                .build();

        ResponseEntity<String> loginResponseEntity = restTemplate.exchange(loginRequest, String.class);

        // e.g. Set-Cookie -> "SESSION=OTY3ZjJmNTYtZjkzZS00YTkyLTkwNDctZjA3NDU0MmI4MmUx; Path=/; HttpOnly; SameSite=Lax"
        String cookieHeaderValue = loginResponseEntity.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertThat("cookie header present", cookieHeaderValue, is(notNullValue()));
        assertThat("cookie header value is not blank", cookieHeaderValue, is(not(blankOrNullString())));
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
         * That's why it will respond with 200 OK (instead of 3xx with 'Location' header).
         */
        RequestEntity<Void> sessionMigrateRequest = RequestEntity.get(lnurlAuthSessionLoginPath())
                .header(HttpHeaders.COOKIE, "SESSION=" + sessionId)
                .build();
        ResponseEntity<Object> sessionMigrateRequestResponseEntity = restTemplate.exchange(sessionMigrateRequest, Object.class);

        assertThat(sessionMigrateRequestResponseEntity.getStatusCode(), is(HttpStatus.OK));

        // we have enabled "migrate session" in spring security and validate this behavior
        String migratedCookieHeaderValue = sessionMigrateRequestResponseEntity.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertThat("migrated cookie header present", migratedCookieHeaderValue, is(notNullValue()));
        assertThat("cookie header value is not blank", migratedCookieHeaderValue, is(not(blankOrNullString())));
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

    @Test
    void lnurlAuthLoginSuccessApiUser() {
        /*
         * STEP 1: Create a session for web user (containing a newly created 'k1' value)
         *
         * Create a session with k1 value and return a bech32 encoded lnurl-auth string.
         * Only the browser knowing the session ID can log in after a wallet signed the k1 value.
         *
         * This is done in a browser.
         * A login page can, e.g. display a qr-code for wallets to scan.
         */
        ResponseEntity<String> loginResponseEntity = restTemplate.exchange(RequestEntity
                .get(lnurlAuthLoginPagePath())
                .build(), String.class);

        String sessionId = LnurlAuthFlowTestHelper.parseSessionIdFromCookie(loginResponseEntity.getHeaders())
                .orElseThrow(() -> new IllegalStateException("Could not find sessionId"));

        LnurlAuth lnurlAuth = LnurlAuthFlowTestHelper.parseFirstLnurlAuthStringInText(loginResponseEntity.getBody())
                .orElseThrow(() -> new IllegalStateException("Could not find lnurl-auth string"));

        SignedLnurlAuth signedLnurlAuth = testWallet.authorize(lnurlAuth);

        // assert that the user still cannot see any guarded resource
        ResponseEntity<Object> authTestRequest0ResponseEntity = restTemplate.exchange(RequestEntity
                .get("/api/v1/authenticated/self")
                .header(HttpHeaders.COOKIE, "SESSION=" + sessionId)
                .build(), Object.class);
        assertThat("user cannot see any guarded resource", authTestRequest0ResponseEntity.getStatusCode(), is(HttpStatus.FORBIDDEN));

        /*
         * STEP 2: Login with wallet
         *
         * Call url in lnurl-auth string with needed value (k1, sig, key)
         * This step is done by the wallet.
         * Most likely based on a scanned qr code.
         */
        ResponseEntity<Object> walletLoginResponseEntity = restTemplate.getForEntity(UriComponentsBuilder
                .fromUri(signedLnurlAuth.toLnurl().toUri())
                .scheme(null).host(null).port(null).build()
                .toUriString(), Object.class);
        assertThat(walletLoginResponseEntity.getStatusCode(), is(HttpStatus.OK));

        // assert that the user still cannot see any guarded resource
        ResponseEntity<Object> authTestRequest1ResponseEntity = restTemplate.exchange(RequestEntity
                .get("/api/v1/authenticated/self")
                .header(HttpHeaders.COOKIE, "SESSION=" + sessionId)
                .build(), Object.class);
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
         * That's why it will respond with 200 OK (instead of 3xx with 'Location' header).
         */
        ResponseEntity<Object> migrateSessionResponse = restTemplate.exchange(RequestEntity
                .get(lnurlAuthSessionLoginPath())
                .header(HttpHeaders.COOKIE, "SESSION=" + sessionId)
                .build(), Object.class);

        assertThat(migrateSessionResponse.getStatusCode(), is(HttpStatus.OK));

        // we have enabled "migrate session" in spring security and validate this behavior
        String migratedSessionId = LnurlAuthFlowTestHelper.parseSessionIdFromCookie(migrateSessionResponse.getHeaders())
                .orElseThrow(() -> new IllegalStateException("Could not find migrated sessionId"));

        /*
         * STEP 4: User is now logged in and can access guarded resources.
         */
        ResponseEntity<String> authTestRequest2ResponseEntity = restTemplate.exchange(RequestEntity
                .get("/api/v1/authenticated/self")
                .header(HttpHeaders.COOKIE, "SESSION=" + migratedSessionId)
                .build(), String.class);
        assertThat(authTestRequest2ResponseEntity.getStatusCode(), is(HttpStatus.OK));
    }

    @RequiredArgsConstructor
    public static final class LnurlAuthFlowTestHelper {

        // e.g. Set-Cookie -> "SESSION=OTY3ZjJmNTYtZjkzZS00YTkyLTkwNDctZjA3NDU0MmI4MmUx; Path=/; HttpOnly; SameSite=Lax"
        public static Optional<String> parseSessionIdFromCookie(HttpHeaders headers) {
            return Optional.ofNullable(headers)
                    .map(it -> it.getFirst(HttpHeaders.SET_COOKIE))
                    .map(sessionIdPattern::matcher)
                    .filter(Matcher::find)
                    .map(it -> it.group(1));
        }

        public static Optional<LnurlAuth> parseFirstLnurlAuthStringInText(String text) {
            return Optional.ofNullable(text)
                    .map(it -> Pattern.compile(".*\"lightning:(lnurl1.*)\".*").matcher(it))
                    .filter(Matcher::find)
                    .map(it -> it.group(1))
                    .map(it -> SimpleLnurlAuth.parse(SimpleLnurl.fromBech32(it)));
        }

        @NonNull
        private final TestRestTemplate restTemplate;

        @NonNull
        private final LnurlWallet wallet;

        public Pair<SignedLnurlAuth, String> login() {
            Pair<LnurlAuth, String> lnurlAuthAndSessionId = fetchLnurlAuthAndSessionId();

            SignedLnurlAuth signedLnurlAuth = triggerWalletLogin(lnurlAuthAndSessionId.getFirst());

            String migratedSessionId = triggerSessionMigration(lnurlAuthAndSessionId.getSecond());

            return new Pair<>(signedLnurlAuth, migratedSessionId);
        }

        private Pair<LnurlAuth, String> fetchLnurlAuthAndSessionId() {
            ResponseEntity<String> loginResponseEntity = restTemplate.exchange(RequestEntity
                    .get(lnurlAuthLoginPagePath())
                    .build(), String.class);

            String sessionId = parseSessionIdFromCookie(loginResponseEntity.getHeaders())
                    .orElseThrow(() -> new IllegalStateException("Could not find sessionId"));

            LnurlAuth lnurlAuth = parseFirstLnurlAuthStringInText(loginResponseEntity.getBody())
                    .orElseThrow(() -> new IllegalStateException("Could not find lnurl-auth string"));

            return new Pair<>(lnurlAuth, sessionId);
        }


        private SignedLnurlAuth triggerWalletLogin(LnurlAuth auth) {
            SignedLnurlAuth signedLnurlAuth = wallet.authorize(auth);

            ResponseEntity<Object> walletLoginResponse = restTemplate.getForEntity(UriComponentsBuilder
                    .fromUri(signedLnurlAuth.toLnurl().toUri())
                    .scheme(null).host(null).port(null).build()
                    .toUriString(), Object.class);

            if (!walletLoginResponse.getStatusCode().is2xxSuccessful()) {
                throw new IllegalStateException("Could not login with wallet");
            }

            return signedLnurlAuth;
        }

        private String triggerSessionMigration(String sessionId) {
            ResponseEntity<Object> migrateSessionResponse = restTemplate.exchange(RequestEntity
                    .get(lnurlAuthSessionLoginPath())
                    .header(HttpHeaders.COOKIE, "SESSION=%s".formatted(sessionId))
                    .build(), Object.class);

            if (!migrateSessionResponse.getStatusCode().is2xxSuccessful()) {
                throw new IllegalStateException("Could not migrate session");
            }

            return parseSessionIdFromCookie(migrateSessionResponse.getHeaders())
                    .orElseThrow(() -> new IllegalStateException("Could not find migrated sessionId"));
        }
    }
}
