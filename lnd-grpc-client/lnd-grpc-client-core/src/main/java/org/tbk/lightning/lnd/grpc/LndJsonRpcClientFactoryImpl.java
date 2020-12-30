package org.tbk.lightning.lnd.grpc;

import org.lightningj.lnd.wrapper.AsynchronousLndAPI;
import org.lightningj.lnd.wrapper.SynchronousLndAPI;

public final class LndJsonRpcClientFactoryImpl implements LndJsonRpcClientFactory {

    @Override
    public AsynchronousLndAPI create(LndRpcConfig config) {
        return new AsynchronousLndAPI(
                config.getRpchost(),
                config.getRpcport(),
                config.getSslContext(),
                config.getMacaroonContext());

    }

    @Override
    public SynchronousLndAPI createSync(LndRpcConfig config) {
        return new SynchronousLndAPI(
                config.getRpchost(),
                config.getRpcport(),
                config.getSslContext(),
                config.getMacaroonContext());
    }
}
