package org.tbk.bitcoin.regtest.scenario;

import org.reactivestreams.Publisher;

/**
 * A {@link RegtestAction} represents a unit of work done in a bitcoin regtest network.
 * e.g. In case of bitcoin-core this can mean "mining a block", "sending a transaction", etc.
 * In case of electrum this can mean "receiving a payment", "check if wallet is synchronized", etc.
 *
 * @param <T> the type of element signaled.
 */
public interface RegtestAction<T> extends Publisher<T> {
}
