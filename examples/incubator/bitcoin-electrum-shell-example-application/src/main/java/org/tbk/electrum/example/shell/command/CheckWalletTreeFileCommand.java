package org.tbk.electrum.example.shell.command;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.FileNameUtils;
import org.bitcoinj.base.Coin;
import org.jline.terminal.Terminal;
import org.springframework.shell.standard.*;
import org.tbk.electrum.ElectrumClient;
import org.tbk.electrum.model.Balance;

import java.io.*;
import java.util.Optional;

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
            @ShellOption(value = "show-zero", defaultValue = "true", help = "filter non-zero balances") boolean showZero,
            @ShellOption(value = "write-out", defaultValue = "true", help = "write to an output file") boolean writeOut,
            @ShellOption(value = "out", defaultValue = "", help = "write output to file") String outArg
    ) throws IOException, InterruptedException {
        Terminal terminal = getTerminal();
        terminal.pause(true);

        String outFile = Optional.ofNullable(outArg)
                .filter(it -> !it.isBlank())
                .orElseGet(() -> "%s_out.%s".formatted(
                        FileNameUtils.getBaseName(fileName),
                        FileNameUtils.getExtension(fileName)
                ));

        try (BufferedReader fileReader = new BufferedReader(new FileReader(fileName));
             Writer fileWriter = !writeOut ? OutputStreamWriter.nullWriter() : new FileWriter(outFile);
             PrintWriter printWriter = new PrintWriter(new BufferedWriter(fileWriter))) {

            String line;
            while ((line = fileReader.readLine()) != null) {
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

                printWriter.printf("%s;%s;%s%n", line, total.toFriendlyString(), !total.isZero());
                printWriter.flush();

                terminal.writer().printf("%s;%s;%s%n", line, total.toFriendlyString(), !total.isZero());
                terminal.writer().flush();
            }
        } finally {
            terminal.resume();
        }
    }
}