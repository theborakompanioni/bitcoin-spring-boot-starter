package org.tbk.electrum.example.shell.command;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.bitcoinj.base.Coin;
import org.springframework.shell.core.command.CommandContext;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.CommandGroup;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;
import org.tbk.electrum.ElectrumClient;
import org.tbk.electrum.model.Balance;

import java.io.*;
import java.util.Optional;

@Slf4j
@Component
@CommandGroup(name = "Commands")
@RequiredArgsConstructor
class CheckWalletTreeFileCommand {

    @NonNull
    private final ElectrumClient client;

    @Command(name = "check-wallet-tree-file", description = "check balances of wallet tree file")
    public void run(
            @Option(longName = "file", description = "the file") String fileName,
            @Option(longName = "show-zero", defaultValue = "true", description = "filter non-zero balances") boolean showZero,
            @Option(longName = "write-out", defaultValue = "true", description = "write to an output file") boolean writeOut,
            @Option(longName = "out", defaultValue = "", description = "write output to file") String outArg,
            CommandContext commandContext
    ) throws IOException, InterruptedException {

        String outFile = Optional.ofNullable(outArg)
                .filter(it -> !it.isBlank())
                .orElseGet(() -> "%s_out.%s".formatted(
                        FilenameUtils.removeExtension(fileName),
                        FilenameUtils.getExtension(fileName)
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

                commandContext.outputWriter().printf("%s;%s;%s%n", line, total.toFriendlyString(), !total.isZero());
                commandContext.outputWriter().flush();
            }
        }
    }
}