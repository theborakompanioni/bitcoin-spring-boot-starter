package org.tbk.bitcoin.example.payreq.lnd.info;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jmolecules.ddd.annotation.Service;
import org.lightningj.lnd.wrapper.message.GetInfoResponse;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Service
@RequiredArgsConstructor
class LndInfoServiceImpl implements LndInfoService {

    @NonNull
    private final LndInfos lndInfos;

    @TransactionalEventListener
    void on(LndInfo.LndInfoCreatedEvent event) {
        LndInfo domain = lndInfos.findById(event.getDomainId())
                .orElseThrow(() -> new EmptyResultDataAccessException(1));

        log.info("[lnd] block #{}: {}", domain.getBlockHeight(), domain.getBlockHash());
    }

    @Override
    @Transactional
    public void createLndInfo(GetInfoResponse info) {
        LndInfo lndInfo = new LndInfo(info.getBlockHeight(),
                info.getBlockHash(),
                info.getBestHeaderTimestamp());

        lndInfos.save(lndInfo);
    }
}
