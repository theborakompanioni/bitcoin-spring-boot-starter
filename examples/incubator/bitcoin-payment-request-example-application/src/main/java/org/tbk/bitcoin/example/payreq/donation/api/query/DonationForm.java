package org.tbk.bitcoin.example.payreq.donation.api.query;

import lombok.Data;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.tbk.bitcoin.example.payreq.common.Network;

import javax.annotation.Nullable;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.util.Optional;

@Data
public class DonationForm {
    @NotNull
    @DecimalMin(value = "1")
    @DecimalMax(value = "20999999.97690000")
    @Digits(integer = 8, fraction = 8)
    private BigDecimal amount;

    @NotNull
    private String currency;

    @Nullable
    @Pattern(regexp = "(mainnet|testnet|regtest)")
    private String network;

    @Nullable
    @Size(max = 255)
    private String comment;

    public Optional<String> getNetwork() {
        return Optional.ofNullable(network);
    }

    public Optional<String> getComment() {
        return Optional.ofNullable(comment);
    }

    public NetworkParameters getBitcoinjNetwork() {
        return getNetwork()
                .flatMap(Network::ofNullable)
                .orElseGet(MainNetParams::get);
    }
}
