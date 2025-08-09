package org.tbk.electrum.example.shell.command;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Coin;
import org.jline.terminal.Terminal;
import org.springframework.shell.standard.*;
import org.tbk.electrum.ElectrumClient;
import org.tbk.electrum.model.Balance;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

@Slf4j
@ShellComponent
@ShellCommandGroup("Commands")
@RequiredArgsConstructor
class CheckAddressesFromFileCommand extends AbstractShellComponent {

    @NonNull
    private final ElectrumClient client;

    @ShellMethod(key = "checkaddressesfromfile", value = "check balances of addresses from a file")
    public void run(
            @ShellOption(value = "file", help = "the file") String fileName
    ) throws IOException {
        Terminal terminal = getTerminal();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isBlank() || line.startsWith("#")) {
                    continue;
                }

                Balance addressBalance = client.getAddressBalance(line);
                Coin total = Coin.ofSat(addressBalance.getTotal().getValue());

                terminal.writer().printf("%s;%s;%s%n", line, total.toFriendlyString(), !total.isZero());
                terminal.writer().flush();
            }
        }
    }
}