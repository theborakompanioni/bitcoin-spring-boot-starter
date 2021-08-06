package org.tbk.spring.lnurl.security;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.tbk.lnurl.auth.LinkingKey;
import org.tbk.lnurl.auth.LnurlAuth;
import org.tbk.lnurl.auth.SignedLnurlAuth;
import org.tbk.lnurl.simple.SimpleLnurl;
import org.tbk.lnurl.simple.auth.SimpleLinkingKey;
import org.tbk.lnurl.simple.auth.SimpleLnurlAuth;
import org.tbk.lnurl.test.SimpleLnurlWallet;
import org.tbk.spring.lnurl.security.test.app.LnurlAuthTestApplication;

import java.security.SecureRandom;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(
        webEnvironment = WebEnvironment.RANDOM_PORT,
        classes = LnurlAuthTestApplication.class
)
@AutoConfigureMockMvc(print = MockMvcPrint.LOG_DEBUG, printOnlyOnFailure = false)
@ActiveProfiles("test")
class LnurlAuthFlowTest {

    @Autowired
    private MockMvc mockMvc;

    private static SimpleLnurlWallet testWallet;

    @BeforeAll
    static void setUpAll() {
        byte[] seed = new SecureRandom().generateSeed(256);
        testWallet = SimpleLnurlWallet.fromSeed(seed);
    }

    @Test
    void lnurlAuthLoginSuccess() throws Exception {
        /*
         * STEP 1: Create a session for web user (containing a newly created 'k1' value)
         *
         * Create a session with k1 value and return an bech32 encoded lnurl-auth string.
         * Only the browser knowing the session ID can login after a wallet signed the k1 value.
         *
         * This is done in a browser.
         * A login page can, e.g. display an qr-code for wallets to scan.
         */
        MvcResult loginResult = mockMvc.perform(get("/login"))
                .andReturn();

        MockHttpSession beforeLoginCompletedSession = Optional.ofNullable(loginResult.getRequest().getSession(false))
                .map(it -> (MockHttpSession) it)
                .orElseThrow(() -> new IllegalStateException("Could not find session."));

        String body = loginResult.getResponse().getContentAsString();
        LnurlAuth lnurlAuth = SimpleLnurlAuth.parse(SimpleLnurl.fromBech32(body));

        SignedLnurlAuth signedLnurlAuth = testWallet.authorize(lnurlAuth);

        /*
         * STEP 2: Login with wallet
         *
         * Call url in lnurl-auth string with needed value (k1, sig, key)
         * This step is done by the wallet.
         * Most likely based on a scanned qr code.
         */
        mockMvc.perform(get(signedLnurlAuth.toLnurl().toUri()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("OK")));

        // assert that the user still cannot see any guarded resource
        mockMvc.perform(get("/authenticated")
                .sessionAttr(LnurlAuthConfigurer.defaultSessionK1Key(), lnurlAuth.getK1().toHex()))
                .andExpect(status().isForbidden());

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
         */
        MvcResult migrateResult = mockMvc.perform(get(LnurlAuthConfigurer.defaultSessionLoginUrl())
                .sessionAttr(LnurlAuthConfigurer.defaultSessionK1Key(), lnurlAuth.getK1().toHex()))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string(HttpHeaders.LOCATION, is("/")))
                .andExpect(redirectedUrl("/"))
                .andReturn();

        MockHttpSession afterLoginCompletedSession = Optional.ofNullable(migrateResult.getRequest().getSession(false))
                .map(it -> (MockHttpSession) it)
                .orElseThrow(() -> new IllegalStateException("Could not find session."));

        // we have enabled "migrate session" in spring security and validate this behavior
        assertThat("Session has been migrated", afterLoginCompletedSession.getId(), is(not(beforeLoginCompletedSession.getId())));

        /*
         * STEP 4: User is now logged in an can access guarded resources.
         */
        // authenticated has been setup to require role "LNURL_AUTH_TEST_USER"
        MvcResult authenticatedResult = mockMvc.perform(get("/authenticated")
                .session(afterLoginCompletedSession))
                .andExpect(status().isOk())
                .andReturn();

        LinkingKey authenticatedKey = SimpleLinkingKey.fromHexStrict(authenticatedResult.getResponse().getContentAsString());
        assertThat("Web user has been authenticated with wallet linking key", authenticatedKey, is(signedLnurlAuth.getLinkingKey()));
    }
}