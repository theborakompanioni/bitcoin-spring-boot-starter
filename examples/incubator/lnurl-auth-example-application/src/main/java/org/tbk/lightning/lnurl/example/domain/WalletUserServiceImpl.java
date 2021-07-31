package org.tbk.lightning.lnurl.example.domain;

import fr.acinq.secp256k1.Hex;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jmolecules.ddd.annotation.Service;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.tbk.lnurl.K1;

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
    public WalletUser findUserOrCreateIfMissing(byte[] linkingKey) {
        Optional<LinkingKey> linkingKeyOrEmpty = linkingKeys.findByLinkingKey(linkingKey);
        if (linkingKeyOrEmpty.isEmpty()) {
            return new WalletUser(new LinkingKey(linkingKey));
        }

        return users.findByLinkingKey(linkingKeyOrEmpty.get())
                .orElseThrow(() -> new EmptyResultDataAccessException("WalletUser key not found", 1));
    }

    @Override
    @Transactional
    public void pairLinkingKeyWithK1(byte[] linkingKey, K1 k1) {
        WalletUser user = findUserOrCreateIfMissing(linkingKey);

        users.save(user.pair(linkingKey, k1));
    }

    @Override
    @Transactional
    public Optional<WalletUser> findByLeastRecentlyUsedK1(K1 k1) {
        return users.findByLeastRecentlyUsedK1(k1);
    }
}
