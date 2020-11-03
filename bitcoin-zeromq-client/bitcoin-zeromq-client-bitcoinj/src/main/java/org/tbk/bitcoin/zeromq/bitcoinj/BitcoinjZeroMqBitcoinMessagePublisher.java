package org.tbk.bitcoin.zeromq.bitcoinj;

import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Transaction;
import org.tbk.bitcoin.zeromq.client.ZeroMqMessagePublishService;

import java.util.concurrent.Flow;

import static java.util.Objects.requireNonNull;

@Slf4j
public final class BitcoinjZeroMqBitcoinMessagePublisher implements Flow.Processor<byte[], Transaction> {



    private final ZeroMqMessagePublishService publisher;

    public BitcoinjZeroMqBitcoinMessagePublisher(ZeroMqMessagePublishService publisher) {
        this.publisher = requireNonNull(publisher);
    }

    @Override
    public void subscribe(Flow.Subscriber<? super Transaction> subscriber) {

    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {

    }

    @Override
    public void onNext(byte[] item) {

    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onComplete() {

    }
}
