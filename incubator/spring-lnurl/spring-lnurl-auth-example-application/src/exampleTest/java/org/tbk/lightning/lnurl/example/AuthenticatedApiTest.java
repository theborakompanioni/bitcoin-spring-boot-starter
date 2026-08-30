package org.tbk.lightning.lnurl.example;

import kotlin.Pair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.tbk.lnurl.auth.SignedLnurlAuth;
import org.tbk.lnurl.test.SimpleLnurlWallet;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.security.SecureRandom;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(
        webEnvironment = WebEnvironment.RANDOM_PORT,
        classes = LnurlAuthExampleApplication.class
)
@ActiveProfiles("test")
@AutoConfigureTestRestTemplate
class AuthenticatedApiTest {
    private static final String GUARDED_ENDPOINT = "/api/v1/authenticated/self";

    private static final SecureRandom random = new SecureRandom();

    private static SimpleLnurlWallet testWallet;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    @Value("${server.servlet.session.cookie.name}")
    private String sessionCookieName;

    @BeforeAll
    static void setUpAll() {
        byte[] seed = random.generateSeed(256);
        testWallet = SimpleLnurlWallet.fromSeed(seed);
    }

    @Test
    void itShouldFetchAuthenticatedUserJson() {
        ResponseEntity<Object> request0 = restTemplate.exchange(RequestEntity.get(GUARDED_ENDPOINT).build(), Object.class);
        assertThat("user cannot see any guarded resource", request0.getStatusCode(), is(HttpStatus.FORBIDDEN));

        Pair<SignedLnurlAuth, String> signedAuthAndSessionId = new LnurlAuthFlowTest.LnurlAuthFlowTestHelper(restTemplate, testWallet, sessionCookieName).login();

        ResponseEntity<String> authTestRequest2ResponseEntity = restTemplate.exchange(RequestEntity.get(GUARDED_ENDPOINT)
                .header(HttpHeaders.COOKIE, "%s=%s".formatted(sessionCookieName, signedAuthAndSessionId.getSecond()))
                .build(), String.class);
        assertThat(authTestRequest2ResponseEntity.getStatusCode(), is(HttpStatus.OK));

        String username = signedAuthAndSessionId.getFirst().getLinkingKey().toHex();

        JsonNode body = objectMapper.readTree(authTestRequest2ResponseEntity.getBody());
        assertThat(body, is(equalTo(objectMapper.readTree("""
                {
                  "accountNonExpired" : true,
                  "accountNonLocked" : true,
                  "authorities" : [ {
                    "authority" : "ROLE_USER"
                  } ],
                  "credentialsNonExpired" : true,
                  "enabled" : true,
                  "password" : null,
                  "username" : "%s"
                }""".formatted(username)))));
    }
}
