package org.tbk.bitcoin.example.payreq.payment.api.query;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.tbk.bitcoin.example.payreq.common.Network;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Optional;

@Data
public class PaymentRequestQueryParams {

    @NotNull(message = "'address' must not be null")
    @NotBlank(message = "'address' must not be empty")
    private String address;

    @Nullable
    @DecimalMin(value = "0.00000001")
    @DecimalMax(value = "20999999.97690000")
    private BigDecimal amount;

    @Nullable
    @Size(max = 64)
    private String label;

    @Nullable
    @Pattern(regexp = "(mainnet|testnet|regtest)")
    private String network;

    public Optional<String> getNetwork() {
        return Optional.ofNullable(network);
    }

    public Optional<String> getAmount() {
        return Optional.ofNullable(amount)
                .map(BigDecimal::toPlainString);
    }

    public Optional<String> getLabel() {
        return Optional.ofNullable(label);
    }

    public Address getBitcoinjAddress() {
        return Address.fromString(getBitcoinjNetwork(), address);
    }

    public NetworkParameters getBitcoinjNetwork() {
        return getNetwork()
                .flatMap(Network::ofNullable)
                .orElseGet(MainNetParams::get);
    }

    public Optional<Coin> getBitcoinjAmount() {
        return getAmount()
                .map(Coin::parseCoin);
    }
}
