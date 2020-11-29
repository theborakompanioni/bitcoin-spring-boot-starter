package org.tbk.lightning.lnd.jsonrpc;

import org.lightningj.lnd.wrapper.AsynchronousLndAPI;
import org.lightningj.lnd.wrapper.SynchronousLndAPI;

public interface LndJsonRpcClientFactory {

    AsynchronousLndAPI create(LndRpcConfig config);

    SynchronousLndAPI createSync(LndRpcConfig config);

}
