package org.tbk.lightning.cln.jsonrpc;

import jrpc.clightning.CLightningRPC;

public class ClnJsonRpcClientFactory {

    public Object justATest() {
        // TODO: properly use CLightningRPC
        return CLightningRPC.getInstance();
    }
}
