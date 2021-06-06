package org.tbk.bitcoin.example.payreq.api;

import org.bitcoinj.core.Coin;
import org.tbk.bitcoin.example.payreq.api.query.PaymentRequestQueryParams;

public final class PaymentRequestUrlHelper {
    private PaymentRequestUrlHelper() {
        throw new UnsupportedOperationException();
    }

    public static String toPaymentUrl(PaymentRequestQueryParams params) {
        // String.format("bitcoin:%s?amount={}&label={}");

        String amountQueryOrEmpty = params.getBitcoinjAmount()
                .map(Coin::toPlainString)
                .map(it -> "amount=" + it)
                .orElse("");

        String query = amountQueryOrEmpty.isBlank() ? "" : "?" + amountQueryOrEmpty;
        return String.format("bitcoin:%s%s", params.getBitcoinjAddress(), query);
    }
}
