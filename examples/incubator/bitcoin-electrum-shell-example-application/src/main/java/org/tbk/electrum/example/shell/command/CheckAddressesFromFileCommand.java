package org.tbk.electrum.example.shell.command;

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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

@Slf4j
@ShellComponent
@ShellCommandGroup("Commands")
@RequiredArgsConstructor
class CheckAddressesFromFileCommand {

    @NonNull
    private final ElectrumClient client;

    @ShellMethod(key = "checkaddressesfromfile", value = "check balances of addresses from a file")
    public String run(
            @ShellOption(value = "file", help = "the file") String fileName
    ) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isBlank() || line.startsWith("#")) {
                    continue;
                }

                Balance addressBalance = client.getAddressBalance(line);
                Coin total = Coin.ofSat(addressBalance.getTotal().getValue());
                System.out.printf("%s;%s%n", line, total.toFriendlyString());
            }
        }
        return "";
    }
}