package org.tbk.spring.lnurl.security;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.tbk.lnurl.auth.K1;
import org.tbk.lnurl.auth.K1Manager;
import org.tbk.lnurl.auth.LinkingKey;
import org.tbk.lnurl.auth.LnurlAuthPairingService;
import org.tbk.lnurl.test.SimpleLnWallet;
import org.tbk.spring.lnurl.security.test.app.LnurlAuthTestApplication;

import java.net.URI;
import java.security.SecureRandom;

import static org.hamcrest.Matchers.is;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(
        webEnvironment = WebEnvironment.RANDOM_PORT,
        classes = LnurlAuthTestApplication.class
)
@AutoConfigureMockMvc(print = MockMvcPrint.LOG_DEBUG, printOnlyOnFailure = false)
@ActiveProfiles("test")
class LnurlSessionAuthenticationFilterTest {

    @Autowired
    private K1Manager k1Manager;

    @Autowired
    private LnurlAuthPairingService pairingService;

    @Autowired
    private MockMvc mockMvc;

    private static SimpleLnWallet testWallet;

    @BeforeAll
    static void setUpAll() {
        byte[] seed = new SecureRandom().generateSeed(256);
        testWallet = SimpleLnWallet.fromSeed(seed);
    }

    @Test
    void sessionLoginMissingParamsError() throws Exception {
        mockMvc.perform(get(LnurlAuthConfigurer.defaultSessionLoginUrl()))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(content().bytes(new byte[0]));
    }

    @Test
    void sessionLoginSuccessBrowser() throws Exception {
        K1 k1 = k1Manager.create();

        // simulate linking with wallet
        LinkingKey linkingKey = testWallet.deriveLinkingPublicKey(URI.create("https://localhost"));
        pairingService.pairK1WithLinkingKey(k1, linkingKey);

        mockMvc.perform(get(LnurlAuthConfigurer.defaultSessionLoginUrl())
                .sessionAttr(LnurlAuthConfigurer.defaultSessionK1Key(), k1.toHex()))
                .andDo(print())
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"))
                .andExpect(content().bytes(new byte[0]));
    }

    @Test
    void sessionLoginSuccessXhr() throws Exception {
        K1 k1 = k1Manager.create();

        // simulate linking with wallet
        LinkingKey linkingKey = testWallet.deriveLinkingPublicKey(URI.create("https://localhost"));
        pairingService.pairK1WithLinkingKey(k1, linkingKey);

        mockMvc.perform(get(LnurlAuthConfigurer.defaultSessionLoginUrl())
                .sessionAttr(LnurlAuthConfigurer.defaultSessionK1Key(), k1.toHex())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.headers").exists())
                .andExpect(jsonPath("$.headers.location").value("/"));
    }

    @Test
    void sessionLoginNoPairingError() throws Exception {
        K1 k1 = k1Manager.create();

        mockMvc.perform(get(LnurlAuthConfigurer.defaultSessionLoginUrl())
                .sessionAttr(LnurlAuthConfigurer.defaultSessionK1Key(), k1.toHex()))
                .andExpect(status().isUnauthorized());
    }
}