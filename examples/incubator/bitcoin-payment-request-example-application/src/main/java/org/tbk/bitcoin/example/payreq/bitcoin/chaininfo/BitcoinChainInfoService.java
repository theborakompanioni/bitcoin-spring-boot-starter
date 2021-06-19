package org.tbk.bitcoin.example.payreq.bitcoin.chaininfo;

import com.msgilligan.bitcoinj.json.pojo.BlockChainInfo;

public interface BitcoinChainInfoService {
    void createChainInfo(BlockChainInfo info);
}
