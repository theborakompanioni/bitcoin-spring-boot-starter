package org.tbk.bitcoin.example.payreq.bitcoin.chaininfo;

import com.google.common.base.MoreObjects;
import org.consensusj.bitcoin.json.pojo.BlockChainInfo;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Utils;
import org.jmolecules.ddd.annotation.Service;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
class BitcoinChainInfoServiceImpl implements BitcoinChainInfoService {

    @NonNull
    private final BitcoinChainInfos chainInfos;

    @TransactionalEventListener
    void on(BitcoinChainInfo.BitcoinChainInfoCreatedEvent event) {
        BitcoinChainInfo domain = chainInfos.findById(event.getDomainId())
                .orElseThrow(() -> new EmptyResultDataAccessException(1));

        log.info("[btc] new chain info (height: {}): {}/{}", domain.getBlocks(), domain.getChain(), domain.getBestBlockHash());
    }

    @Override
    @Transactional
    public void createChainInfo(BlockChainInfo info) {
        String chainWork = Optional.ofNullable(info.getChainWork())
                .map(Utils.HEX::encode)
                .orElse("");

        BitcoinChainInfo bitcoinChainInfo = new BitcoinChainInfo(
                info.getChain(),
                info.getBlocks(),
                info.getHeaders(),
                info.getBestBlockHash(),
                MoreObjects.firstNonNull(info.getDifficulty(), BigDecimal.ZERO).toPlainString(),
                MoreObjects.firstNonNull(info.getVerificationProgress(), BigDecimal.ZERO).toPlainString(),
                chainWork);

        chainInfos.save(bitcoinChainInfo);
    }
}
