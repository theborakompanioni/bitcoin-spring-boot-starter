package org.tbk.bitcoin.example.payreq.payment.api.query;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.bitcoinj.base.Address;
import org.bitcoinj.base.Coin;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.tbk.bitcoin.example.payreq.common.Network;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Optional;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class PaymentRequestQueryParams {

    @NotNull(message = "'address' must not be null")
    @NotBlank(message = "'address' must not be empty")
    String address;

    @Nullable
    @DecimalMin(value = "0.00000001")
    @DecimalMax(value = "20999999.97690000")
    BigDecimal amount;

    @Nullable
    @Size(max = 64)
    String label;

    @Nullable
    @Pattern(regexp = "(mainnet|testnet|regtest)")
    String network;

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

    @JsonIgnore
    public Address getBitcoinjAddress() {
        return Address.fromString(getBitcoinjNetwork(), address);
    }

    @JsonIgnore
    public NetworkParameters getBitcoinjNetwork() {
        return getNetwork()
                .flatMap(Network::ofNullable)
                .orElseGet(MainNetParams::get);
    }

    @JsonIgnore
    public Optional<Coin> getBitcoinjAmount() {
        return getAmount()
                .map(Coin::parseCoin);
    }
}
