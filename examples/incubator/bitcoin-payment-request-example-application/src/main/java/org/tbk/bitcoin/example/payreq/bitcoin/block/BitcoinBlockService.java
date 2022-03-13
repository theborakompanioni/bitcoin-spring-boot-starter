package org.tbk.bitcoin.example.payreq.bitcoin.block;

import org.consensusj.bitcoin.json.pojo.BlockInfo;

public interface BitcoinBlockService {
    void createBlock(BlockInfo blockInfo);

    void updatePreviousBlockIfPresent(BitcoinBlock.BitcoinBlockId parentBlockId);
}
