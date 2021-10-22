package org.tbk.lightning.lnurl.example.domain;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.tbk.lnurl.simple.auth.SimpleLinkingKey;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


@SpringBootTest
@ActiveProfiles("test")
class WalletUserServiceImplIntegrationTest {

    @Autowired
    private WalletUserService walletUserService;

    @Test
    @Transactional
    void itShouldCreateNewUserIfMissing() {
        String validLinkingKeyHex = "0465d6177992064a24c24213230a0c3eeb5f2047c7286391c7ead608cda473f787af9afcae9af3a6a84f28a775ad257dbf6027448461455aaf482569237dda27bd";

        SimpleLinkingKey linkingKey = SimpleLinkingKey.fromHexStrict(validLinkingKeyHex);

        assertThat(walletUserService.findUser(linkingKey), is(Optional.empty()));

        WalletUser walletUser = walletUserService.findUserOrCreateIfMissing(linkingKey);
        assertThat(walletUser.getId(), is(notNullValue()));

        WalletUser refetchedUser = walletUserService.findUser(linkingKey).orElseThrow();
        assertThat(refetchedUser.getId(), is(walletUser.getId()));

        List<AuthLinkingKey> linkingKeys = refetchedUser.getLinkingKeys();
        assertThat(linkingKeys, hasSize(1));

        AuthLinkingKey authLinkingKey = linkingKeys.get(0);
        assertThat(authLinkingKey.getLinkingKey(), is(validLinkingKeyHex));
        assertThat(authLinkingKey.toPublicKey().isValid(), is(true));
    }

}
