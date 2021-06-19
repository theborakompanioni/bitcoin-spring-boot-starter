package org.tbk.bitcoin.example.payreq.lnd.info;

import org.lightningj.lnd.wrapper.message.GetInfoResponse;

public interface LndInfoService {
    void createLndInfo(GetInfoResponse info);
}
