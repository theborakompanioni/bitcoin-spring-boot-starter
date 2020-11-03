package org.tbk.bitcoin.zeromq.bitcoinj;

import java.util.concurrent.Executor;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public class TransformProcessor<S, T> extends SubmissionPublisher<T> implements Flow.Processor<S, T> {
    private final Function<? super S, ? extends T> function;
    private Flow.Subscription subscription;

    TransformProcessor(Executor executor, int maxBufferCapacity,
                       Function<? super S, ? extends T> function) {
        super(executor, maxBufferCapacity);
        this.function = requireNonNull(function);
    }

    public void onSubscribe(Flow.Subscription subscription) {
        this.subscription = subscription;
        this.subscription.request(1);
    }

    public void onNext(S item) {
        this.subscription.request(1);
        this.submit(this.function.apply(item));
    }

    public void onError(Throwable ex) {
        this.closeExceptionally(ex);
    }

    public void onComplete() {
        this.close();
    }
}
