package org.tbk.lightning.lnurl.example.domain;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jmolecules.ddd.annotation.Service;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.tbk.lnurl.auth.K1;
import org.tbk.lnurl.auth.LinkingKey;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
class WalletUserServiceImpl implements WalletUserService {

    @NonNull
    private final WalletUsers users;

    @NonNull
    private final AuthLinkingKeys linkingKeys;

    @TransactionalEventListener
    void on(WalletUser.WalletUserCreatedEvent event) {
        WalletUser domain = users.findById(event.getDomainId())
                .orElseThrow(() -> new EmptyResultDataAccessException("WalletUser key not found", 1));

        log.info("Received application event '{}': {}", event.getClass().getSimpleName(), domain);
    }

    @Override
    @Transactional
    public Optional<WalletUser> findUser(LinkingKey linkingKey) {
        return users.findByLinkingKey(linkingKey.toHex());
    }

    @Override
    @Transactional
    public WalletUser findUserOrCreateIfMissing(LinkingKey linkingKey) {
        Optional<AuthLinkingKey> linkingKeyOrEmpty = linkingKeys.findByLinkingKey(linkingKey);
        if (linkingKeyOrEmpty.isEmpty()) {
            return users.save(new WalletUser(new AuthLinkingKey(linkingKey)));
        }

        return users.findByLinkingKey(linkingKeyOrEmpty.get())
                .orElseThrow(() -> new EmptyResultDataAccessException("WalletUser key not found", 1));
    }

    @Override
    @Transactional
    public void pairLinkingKeyWithK1(LinkingKey linkingKey, K1 k1) {
        WalletUser user = findUserOrCreateIfMissing(linkingKey);

        users.save(user.pair(linkingKey, k1));
    }

    @Override
    @Transactional
    public Optional<WalletUser> findByLeastRecentlyUsedK1(K1 k1) {
        return users.findByLeastRecentlyUsedK1(k1);
    }
}
