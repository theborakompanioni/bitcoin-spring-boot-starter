package org.tbk.bitcoin.example.payreq.bitcoin.block;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jmolecules.ddd.annotation.Service;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Service
@RequiredArgsConstructor
class BitcoinBlockEventListener {

    @NonNull
    private final BitcoinBlockService blockService;

    @NonNull
    private final BitcoinBlocks blocks;

    @TransactionalEventListener
    void onCreatedEvent(BitcoinBlock.BitcoinBlockCreatedEvent event) {
        BitcoinBlock domain = blocks.findById(event.getDomainId())
                .orElseThrow(() -> new EmptyResultDataAccessException(1));

        log.info("[btc] new block #{}: {}", domain.getHeight(), domain);
    }

    @Async
    @TransactionalEventListener
    void onConfirmationEvent(BitcoinBlock.BitcoinBlockConfirmationEvent event) {
        BitcoinBlock domain = blocks.findById(event.getDomainId())
                .orElseThrow(() -> new EmptyResultDataAccessException(1));

        log.info("confirmation event: block #{} has {} confirmations", domain.getHeight(), domain.getConfirmations());

        if (domain.getConfirmations() < 6) {
            blockService.updatePreviousBlockIfPresent(domain.getId());
        }
    }

}
