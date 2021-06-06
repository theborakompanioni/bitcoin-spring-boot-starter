package org.tbk.bitcoin.example.payreq.api.query;

import lombok.Data;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;

import javax.annotation.Nullable;
import javax.validation.constraints.*;
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
    @Size(min = 0, max = 64)
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
                .flatMap(NetworkQueryParam::ofNullable)
                .orElseGet(MainNetParams::get);
    }

    public Optional<Coin> getBitcoinjAmount() {
        return getAmount()
                .map(Coin::parseCoin);
    }

    /*public static final class PaymentRequestQueryParamsValidator implements Validator {
        @Override
        public boolean supports(Class<?> clazz) {
            return clazz == PaymentRequestQueryParams.class;
        }

        @Override
        public void validate(Object target, Errors errors) {
            PaymentRequestQueryParams properties = (PaymentRequestQueryParams) target;


        }
    }*/
}
