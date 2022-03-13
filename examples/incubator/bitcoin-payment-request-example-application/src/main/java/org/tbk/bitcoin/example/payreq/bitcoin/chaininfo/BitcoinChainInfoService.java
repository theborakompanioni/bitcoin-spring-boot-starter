package org.tbk.bitcoin.example.payreq.bitcoin.chaininfo;

import org.consensusj.bitcoin.json.pojo.BlockChainInfo;

public interface BitcoinChainInfoService {
    void createChainInfo(BlockChainInfo info);
}
