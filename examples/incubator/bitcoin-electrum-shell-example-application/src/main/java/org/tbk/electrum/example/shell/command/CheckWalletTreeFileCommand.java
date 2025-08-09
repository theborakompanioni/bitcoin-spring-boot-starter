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
class CheckWalletTreeFileCommand extends AbstractShellComponent {

    @NonNull
    private final ElectrumClient client;

    @ShellMethod(key = "check-wallet-tree-file", value = "check balances of wallet tree file")
    public void run(
            @ShellOption(value = "file", help = "the file") String fileName,
            @ShellOption(value = "show-zero", defaultValue = "true", help = "filter non-zero balances") boolean showZero
    ) throws IOException, InterruptedException {
        Terminal terminal = getTerminal();
        terminal.pause(true);

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("#") || line.startsWith("//")) {
                    continue;
                }
                String address = line.split(";", 2)[0].trim();
                if (address.isBlank()) {
                    continue;
                }

                Balance addressBalance = client.getAddressBalance(address);
                Coin total = Coin.ofSat(addressBalance.getTotal().getValue());

                if (!showZero && total.isZero()) {
                    continue;
                }

                terminal.writer().printf("%s;%s;%s%n", line, total.toFriendlyString(), !total.isZero());
                terminal.writer().flush();
            }
        } finally {
            terminal.resume();
        }
    }
}