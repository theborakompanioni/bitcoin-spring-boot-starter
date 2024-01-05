package org.tbk.bitcoin.example.payreq.lnd.info;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jmolecules.ddd.annotation.Service;
import org.lightningj.lnd.wrapper.StatusException;
import org.lightningj.lnd.wrapper.SynchronousLndAPI;
import org.lightningj.lnd.wrapper.ValidationException;
import org.lightningj.lnd.wrapper.message.GetInfoResponse;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.tbk.bitcoin.example.payreq.bitcoin.block.BitcoinBlock;

@Slf4j
@Service
@RequiredArgsConstructor
class LndInfoUpdateListener {

    @NonNull
    private final SynchronousLndAPI lndApi;

    @NonNull
    private final LndInfoService lndInfoService;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener
    void on(BitcoinBlock.BitcoinBlockCreatedEvent event) {
        try {
            GetInfoResponse info = lndApi.getInfo();
            lndInfoService.createLndInfo(info);
        } catch (StatusException | ValidationException e) {
            log.error("error while fetching 'info' via lnd api", e);
        } catch (Exception e) {
            log.error("", e);
        }
    }
}
