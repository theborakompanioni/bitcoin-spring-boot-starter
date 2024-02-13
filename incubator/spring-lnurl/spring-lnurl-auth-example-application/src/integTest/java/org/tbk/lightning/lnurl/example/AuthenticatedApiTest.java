package org.tbk.lightning.lnurl.example;

import kotlin.Pair;
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
import org.tbk.lnurl.auth.SignedLnurlAuth;
import org.tbk.lnurl.test.SimpleLnurlWallet;

import java.security.SecureRandom;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(
        webEnvironment = WebEnvironment.RANDOM_PORT,
        classes = LnurlAuthExampleApplication.class
)
@ActiveProfiles("test")
class AuthenticatedApiTest {
    private static final SecureRandom random = new SecureRandom();

    private static SimpleLnurlWallet testWallet;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeAll
    static void setUpAll() {
        byte[] seed = random.generateSeed(256);
        testWallet = SimpleLnurlWallet.fromSeed(seed);
    }

    @Test
    void itShouldFetchAuthenticatedUserJson() {
        ResponseEntity<Object> request0 = restTemplate.exchange(RequestEntity
                .get("/api/v1/authenticated/self")
                .build(), Object.class);
        assertThat("user cannot see any guarded resource", request0.getStatusCode(), is(HttpStatus.FORBIDDEN));

        Pair<SignedLnurlAuth, String> signedAuthAndSessionId = new LnurlAuthFlowTest.LnurlAuthFlowTestHelper(restTemplate, testWallet).login();

        ResponseEntity<String> authTestRequest2ResponseEntity = restTemplate.exchange(RequestEntity
                .get("/api/v1/authenticated/self")
                .header(HttpHeaders.COOKIE, "SESSION=%s".formatted(signedAuthAndSessionId.getSecond()))
                .build(), String.class);
        assertThat(authTestRequest2ResponseEntity.getStatusCode(), is(HttpStatus.OK));

        assertThat(authTestRequest2ResponseEntity.getBody(), is("""
                {
                  "username" : "%s",
                  "authorities" : [ {
                    "authority" : "ROLE_USER"
                  } ],
                  "accountNonExpired" : true,
                  "accountNonLocked" : true,
                  "credentialsNonExpired" : true,
                  "enabled" : true
                }""".formatted(signedAuthAndSessionId.getFirst().getLinkingKey().toHex())));
    }
}
