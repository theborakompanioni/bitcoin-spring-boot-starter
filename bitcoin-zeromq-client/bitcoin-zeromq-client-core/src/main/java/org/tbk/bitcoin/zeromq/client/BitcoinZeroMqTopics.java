package org.tbk.bitcoin.zeromq.client;


import java.util.Optional;
import java.util.stream.Stream;

/**
 * Bitcoin ZeroMQ topic names.
 *
 * <p>zmqpubhashtx : Publishes transaction hashes
 * zmqpubhashblock : Publishes block hashes
 * zmqpubrawblock : Publishes raw block information
 * zmqpubrawtx : Publishes raw transaction information
 */
public final class BitcoinZeroMqTopics {

    private static final Topic rawtx = Topic.SimpleTopic.of("rawtx");

    private static final Topic rawblock = Topic.SimpleTopic.of("rawblock");

    private static final Topic hashtx = Topic.SimpleTopic.of("hashtx");

    private static final Topic hashblock = Topic.SimpleTopic.of("hashblock");

    public static Optional<Topic> ofName(String name) {
        return Stream.of(rawtx, rawblock, hashtx, hashblock)
                .filter(val -> val.getName().equals(name))
                .findFirst();
    }

    public static Topic rawtx() {
        return rawtx;
    }

    public static Topic rawblock() {
        return rawblock;
    }

    public static Topic hashtx() {
        return hashtx;
    }

    public static Topic hashblock() {
        return hashblock;
    }

    private BitcoinZeroMqTopics() {
        throw new UnsupportedOperationException();
    }

}
