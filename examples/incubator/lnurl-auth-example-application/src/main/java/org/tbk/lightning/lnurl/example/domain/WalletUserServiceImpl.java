package org.tbk.lightning.lnurl.example.domain;

import fr.acinq.secp256k1.Hex;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jmolecules.ddd.annotation.Service;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.tbk.lightning.lnurl.example.lnurl.security.LnurlAuthSessionToken;
import org.tbk.lightning.lnurl.example.lnurl.security.LnurlAuthWalletToken;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
class WalletUserServiceImpl implements WalletUserService {

    @NonNull
    private final WalletUsers users;

    @NonNull
    private final LinkingKeys linkingKeys;

    @TransactionalEventListener
    void on(WalletUser.WalletUserCreatedEvent event) {
        WalletUser domain = users.findById(event.getDomainId())
                .orElseThrow(() -> new EmptyResultDataAccessException("WalletUser key not found", 1));

        log.info("Received application event: {}", domain);
    }

    @Override
    @Transactional
    public Optional<WalletUser> findUser(byte[] linkingKey) {
        return users.findByLinkingKey(Hex.encode(linkingKey));
    }

    @Override
    @Transactional
    public WalletUser findUserCreateIfMissing(byte[] linkingKey) {
        Optional<LinkingKey> linkingKeyOrEmpty = linkingKeys.findByLinkingKey(linkingKey);
        if (linkingKeyOrEmpty.isEmpty()) {
            return new WalletUser(new LinkingKey(linkingKey));
        }

        return users.findByLinkingKey(linkingKeyOrEmpty.get())
                .orElseThrow(() -> new EmptyResultDataAccessException("WalletUser key not found", 1));
    }

    @Override
    @Transactional
    public void login(LnurlAuthWalletToken auth) {
        if (!auth.isAuthenticated()) {
            throw new IllegalStateException("LnurlAuthWalletToken must be authenticated.");
        }

        WalletUser user = findUserCreateIfMissing(auth.getLinkingKey());

        users.save(user.login(auth));
    }

    @Override
    @Transactional
    public Optional<WalletUser> login(LnurlAuthSessionToken auth) {
        if (auth.isAuthenticated()) {
            throw new IllegalStateException("LnurlAuthSessionToken must not be authenticated.");
        }

        return users.findByLeastRecentlyUsedK1(auth.getK1());
    }
}
