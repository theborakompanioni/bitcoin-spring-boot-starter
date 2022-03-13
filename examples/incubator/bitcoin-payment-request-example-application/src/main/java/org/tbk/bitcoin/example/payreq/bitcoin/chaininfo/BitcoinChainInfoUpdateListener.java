package org.tbk.bitcoin.example.payreq.bitcoin.chaininfo;

import org.consensusj.bitcoin.json.pojo.BlockChainInfo;
import org.consensusj.bitcoin.rpc.BitcoinClient;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jmolecules.ddd.annotation.Service;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionalEventListener;
import org.tbk.bitcoin.example.payreq.bitcoin.block.BitcoinBlock;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
class BitcoinChainInfoUpdateListener {

    @NonNull
    private final BitcoinClient bitcoinJsonRpcClient;

    @NonNull
    private final BitcoinChainInfoService chainInfoService;

    @Async
    @TransactionalEventListener
    void on(BitcoinBlock.BitcoinBlockCreatedEvent event) {
        try {
            BlockChainInfo info = bitcoinJsonRpcClient.getBlockChainInfo();
            chainInfoService.createChainInfo(info);
        } catch (IOException e) {
            log.error("error while fetching 'blockchaininfo' via bitcoin api", e);
        } catch (Exception e) {
            log.error("", e);
        }
    }
}
