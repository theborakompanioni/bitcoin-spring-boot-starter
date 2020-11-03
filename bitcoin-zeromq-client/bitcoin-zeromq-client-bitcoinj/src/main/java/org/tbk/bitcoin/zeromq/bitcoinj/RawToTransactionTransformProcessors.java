package org.tbk.bitcoin.zeromq.bitcoinj;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.params.MainNetParams;

import java.util.concurrent.*;

public final class RawToTransactionTransformProcessors {

    public static Flow.Processor<byte[], Transaction> mainnetTxTransformer(Executor executor, int maxBufferCapacity) {

        return rawToTxTransformer(executor, maxBufferCapacity, MainNetParams.get());
    }

    public static Flow.Processor<byte[], Transaction> rawToTxTransformer(Executor executor,
                                                                         int maxBufferCapacity,
                                                                         NetworkParameters network) {
        return new TransformProcessor<>(executor, maxBufferCapacity, raw -> new Transaction(network, raw));
    }
}
