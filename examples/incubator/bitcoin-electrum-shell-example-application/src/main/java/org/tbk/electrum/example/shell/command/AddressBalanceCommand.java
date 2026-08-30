package org.tbk.electrum.example.shell.command;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.base.Coin;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.CommandGroup;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;
import org.tbk.electrum.ElectrumClient;
import org.tbk.electrum.model.Balance;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@CommandGroup(name = "Commands")
@RequiredArgsConstructor
class AddressBalanceCommand {
    private static final List<String> types = Arrays.asList("total|total_onchain|unconfirmed|confirmed|lightning|spendable|unmatured".split("\\|"));

    @NonNull
    private final ElectrumClient client;

    @Command(name = "getaddressbalance", description = "execute command 'getaddressbalance'")
    public String run(
            @Option(longName = "address", description = "bitcoin address") String address,
            @Option(longName = "type", description = "total|unconfirmed|confirmed", defaultValue = "total") String type
    ) {
        Balance result = client.getAddressBalance(address);

        Coin coin = Coin.ofSat(switch (type) {
            case "unconfirmed" -> result.getUnconfirmed().getValue();
            case "confirmed" -> result.getConfirmed().getValue();
            case "total" -> result.getTotal().getValue();
            default ->
                    throw new IllegalArgumentException("Unknown type, expected %s, got: '%s'".formatted(types, type));
        });

        return coin.toFriendlyString();
    }
}