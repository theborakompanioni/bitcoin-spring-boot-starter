package org.tbk.electrum.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.List;
import java.util.Optional;

@Value
@Builder
public class SimpleAddressWithBalance implements AddressWithBalance {
    public static SimpleAddressWithBalance from(List<String> values) {
        if (values.size() < 2) {
            throw new IllegalArgumentException("'values' must contain at least two entries");
        }
        return SimpleAddressWithBalance.builder()
                .address(values.get(0))
                .balance(BtcTxoValues.fromBtcString(values.get(1)))
                .label(values.size() < 3 ? null : toLabel(values.get(2))
                        .orElse(null))
                .build();
    }

    private static Optional<String> toLabel(String value) {
        return Optional.ofNullable(value)
                .filter(it -> !"''".equals(it))
                .map(it -> !it.startsWith("'") ? it : it.substring(1, it.length() - 1))
                .map(it -> !it.endsWith("'") ? it : it.substring(0, it.length() - 2));
    }

    @NonNull
    String address;

    @NonNull
    TxoValue balance;

    String label;

    @Override
    public Optional<String> getLabel() {
        return Optional.ofNullable(label);
    }
}
