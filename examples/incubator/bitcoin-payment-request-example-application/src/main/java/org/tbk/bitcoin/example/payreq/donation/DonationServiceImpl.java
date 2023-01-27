package org.tbk.bitcoin.example.payreq.donation;

import jakarta.validation.ValidationException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bitcoinj.core.Coin;
import org.javamoney.moneta.Money;
import org.jmolecules.ddd.annotation.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tbk.bitcoin.example.payreq.common.Currencies;
import org.tbk.bitcoin.example.payreq.common.Network;
import org.tbk.bitcoin.example.payreq.donation.api.query.DonationForm;
import org.tbk.bitcoin.example.payreq.order.LineItem;
import org.tbk.bitcoin.example.payreq.order.Order;
import org.tbk.bitcoin.example.payreq.order.OrderService;
import org.tbk.bitcoin.example.payreq.payment.PaymentRequest;
import org.tbk.bitcoin.example.payreq.payment.PaymentRequestService;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.convert.ConversionQueryBuilder;
import javax.money.convert.CurrencyConversion;
import javax.money.convert.MonetaryConversions;
import java.math.BigInteger;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

@Service
@RequiredArgsConstructor
class DonationServiceImpl implements DonationService {

    @NonNull
    private final Donations donations;

    @NonNull
    private final PaymentRequestService paymentRequestService;

    @NonNull
    private final OrderService orderService;

    @Override
    @Transactional
    public Donation create(DonationForm form) {
        Network network = form.getNetwork()
                .map(Network::valueOf)
                .orElse(Network.mainnet);

        CurrencyUnit sourceCurrency = Monetary.getCurrency(form.getCurrency());

        CurrencyConversion toBtcConversion = MonetaryConversions.getConversion(ConversionQueryBuilder.of()
                .setBaseCurrency(sourceCurrency)
                .setTermCurrency(Currencies.BTC)
                .build());

        Money sourceMonetaryAmount = Money.of(form.getAmount(), sourceCurrency);
        Money bitcoinMonetaryAmount = sourceMonetaryAmount.with(toBtcConversion)
                .with(Monetary.getRounding(Currencies.BTC));

        // TODO: externalize payment method provider
        String paymentMethod = form.getPaymentMethod().orElse("lightning");
        if (!"onchain".equals(paymentMethod) && !"lightning".equals(paymentMethod)) {
            String errorMessage = String.format("Unsupported payment method: '%s'", paymentMethod);
            throw new ValidationException(errorMessage);
        }

        BigInteger satoshiMonetaryAmount = bitcoinMonetaryAmount.getNumberStripped().unscaledValue();

        Coin donationAmount = Coin.valueOf(satoshiMonetaryAmount.longValue());

        String lineItemName = "Donation/" + donationAmount.toFriendlyString();
        LineItem lineItem = new LineItem(lineItemName, bitcoinMonetaryAmount);

        Order order = orderService.createOrder(Collections.singletonList(lineItem));

        Instant now = Instant.now();
        Instant paymentRequestValidUntil = now.plus(5, ChronoUnit.MINUTES);

        PaymentRequest paymentRequest = null;
        if ("onchain".equals(paymentMethod)) {
            paymentRequest = paymentRequestService.createOnchainPayment(order, network, paymentRequestValidUntil, 0);
        } else {
            paymentRequest = paymentRequestService.createLightningPayment(order, network, paymentRequestValidUntil);
        }

        String description = String.format("Donation of %s (%s) on %s",
                donationAmount.toFriendlyString(), sourceMonetaryAmount,
                ZonedDateTime.ofInstant(now, ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT));

        Donation donation = new Donation(order, paymentRequest);
        donation.setDescription(description);
        donation.setComment(form.getComment().orElse(null));

        return donations.save(donation);
    }
}
