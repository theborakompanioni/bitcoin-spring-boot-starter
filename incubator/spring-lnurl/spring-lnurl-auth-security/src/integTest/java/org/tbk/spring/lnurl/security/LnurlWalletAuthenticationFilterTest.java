package org.tbk.spring.lnurl.security;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.tbk.lnurl.auth.K1;
import org.tbk.lnurl.auth.K1Manager;
import org.tbk.lnurl.auth.LnurlAuth;
import org.tbk.lnurl.auth.SignedLnurlAuth;
import org.tbk.lnurl.simple.auth.SimpleK1;
import org.tbk.lnurl.simple.auth.SimpleLnurlAuth;
import org.tbk.lnurl.test.SimpleLnurlWallet;
import org.tbk.spring.lnurl.security.test.app.LnurlAuthTestApplication;

import java.net.URI;
import java.security.SecureRandom;

import static org.hamcrest.Matchers.is;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        webEnvironment = WebEnvironment.RANDOM_PORT,
        classes = LnurlAuthTestApplication.class
)
@AutoConfigureMockMvc(print = MockMvcPrint.LOG_DEBUG, printOnlyOnFailure = false)
@ActiveProfiles("test")
class LnurlWalletAuthenticationFilterTest {

    @Autowired
    private K1Manager k1Manager;

    @Autowired
    private MockMvc mockMvc;

    private static SimpleLnurlWallet testWallet;

    @BeforeAll
    static void setUpAll() {
        byte[] seed = new SecureRandom().generateSeed(256);
        testWallet = SimpleLnurlWallet.fromSeed(seed);
    }

    @Test
    void walletLoginMissingParamsError() throws Exception {
        mockMvc.perform(get(LnurlAuthConfigurer.defaultWalletLoginUrl()))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.reason", is("Request could not be authenticated.")));
    }

    @Test
    void walletLoginSuccess() throws Exception {
        URI url = URI.create("https://localhost" + LnurlAuthConfigurer.defaultWalletLoginUrl());
        LnurlAuth lnurlAuth = SimpleLnurlAuth.create(url, k1Manager.create());

        SignedLnurlAuth signedLnurlAuth = testWallet.authorize(lnurlAuth);

        mockMvc.perform(get(signedLnurlAuth.toLnurl().toUri()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("OK")));
    }

    @Test
    void walletLoginInvalidK1() throws Exception {
        URI loginUri = URI.create("https://localhost" + LnurlAuthConfigurer.defaultWalletLoginUrl());
        LnurlAuth lnurlAuth = SimpleLnurlAuth.create(loginUri, k1Manager.create());

        SignedLnurlAuth signedLnurlAuth = testWallet.authorize(lnurlAuth);

        mockMvc.perform(get(loginUri)
                .queryParam(LnurlAuth.LNURL_AUTH_K1_KEY, "00".repeat(32))
                .queryParam(SignedLnurlAuth.LNURL_AUTH_SIG_KEY, signedLnurlAuth.getSignature().toHex())
                .queryParam(SignedLnurlAuth.LNURL_AUTH_KEY_KEY, signedLnurlAuth.getLinkingKey().toHex()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.reason", is("Request could not be authenticated.")));
    }

    @Test
    void walletLoginMismatchingSig() throws Exception {
        URI loginUri = URI.create("https://localhost" + LnurlAuthConfigurer.defaultWalletLoginUrl());

        K1 realK1 = k1Manager.create(); // known to the service - we use it to make the request
        K1 fakeK1 = SimpleK1.fromHex("00".repeat(32)); // unknown to service - we use it to sign the request
        LnurlAuth lnurlAuth = SimpleLnurlAuth.create(loginUri, fakeK1);

        SignedLnurlAuth signedLnurlAuth = testWallet.authorize(lnurlAuth);

        mockMvc.perform(get(loginUri)
                .queryParam(LnurlAuth.LNURL_AUTH_K1_KEY, realK1.toHex())
                .queryParam(SignedLnurlAuth.LNURL_AUTH_SIG_KEY, signedLnurlAuth.getSignature().toHex())
                .queryParam(SignedLnurlAuth.LNURL_AUTH_KEY_KEY, signedLnurlAuth.getLinkingKey().toHex()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.reason", is("Request could not be authenticated.")));
    }

    @Test
    void walletLoginMismatchingKey() throws Exception {
        URI loginUri = URI.create("https://localhost" + LnurlAuthConfigurer.defaultWalletLoginUrl());

        LnurlAuth lnurlAuth = SimpleLnurlAuth.create(loginUri, k1Manager.create());

        SignedLnurlAuth signedLnurlAuth = testWallet.authorize(lnurlAuth);

        String mismatchingKeyHex = signedLnurlAuth.getLinkingKey().toHex().replaceAll("[0-9a-fA-F]", "0");

        mockMvc.perform(get(loginUri)
                .queryParam(LnurlAuth.LNURL_AUTH_K1_KEY, signedLnurlAuth.getK1().toHex())
                .queryParam(SignedLnurlAuth.LNURL_AUTH_SIG_KEY, signedLnurlAuth.getSignature().toHex())
                .queryParam(SignedLnurlAuth.LNURL_AUTH_KEY_KEY, mismatchingKeyHex))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.reason", is("Request could not be authenticated.")));
    }
}