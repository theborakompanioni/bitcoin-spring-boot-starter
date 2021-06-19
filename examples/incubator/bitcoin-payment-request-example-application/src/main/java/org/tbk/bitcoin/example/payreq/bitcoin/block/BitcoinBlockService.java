package org.tbk.bitcoin.example.payreq.bitcoin.block;

import com.msgilligan.bitcoinj.json.pojo.BlockInfo;

public interface BitcoinBlockService {
    void createBlock(BlockInfo blockInfo);

    void updatePreviousBlockIfPresent(BitcoinBlock.BitcoinBlockId parentBlockId);
}
