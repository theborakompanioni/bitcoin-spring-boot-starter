package org.tbk.electrum.shell.example.command;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Coin;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.tbk.electrum.ElectrumClient;
import org.tbk.electrum.model.Balance;

@Slf4j
@ShellComponent
@ShellCommandGroup("Commands")
@RequiredArgsConstructor
class AddressBalanceCommand {
    private static final String[] types = "total|total_onchain|unconfirmed|confirmed|lightning|spendable|unmatured".split("\\|");

    @NonNull
    private final ElectrumClient client;

    @ShellMethod(key = "getaddressbalance", value = "execute command 'getaddressbalance'")
    public String run(
            @ShellOption(value = "address", help = "bitcoin address") String address,
            @ShellOption(value = "type", help = "total|total_onchain|unconfirmed|confirmed|lightning|spendable|unmatured", defaultValue = "total") String type
    ) {
        Balance result = client.getAddressBalance(address);

        Coin coin = Coin.ofSat(switch (type) {
            case "unconfirmed" -> result.getUnconfirmed().getValue();
            case "confirmed" -> result.getConfirmed().getValue();
            case "lightning" -> result.getLightning().getValue();
            case "spendable" -> result.getSpendable().getValue();
            case "unmatured" -> result.getUnmatured().getValue();
            case "total" -> result.getTotalOnChain().getValue();
            case "total_onchain" -> result.getTotal().getValue();
            default ->
                    throw new IllegalArgumentException("Unknown type, expected %s, got: '%s'".formatted(types, type));
        });

        return coin.toFriendlyString();
    }
}