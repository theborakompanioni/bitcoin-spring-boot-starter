package org.tbk.electrum.example.shell.command;

import fr.acinq.bitcoin.Block;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.core.command.CommandContext;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.CommandGroup;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;
import org.tbk.electrum.ElectrumClient;
import org.tbk.electrum.example.shell.util.MoreBlocks;
import org.tbk.electrum.example.shell.util.Wallet;
import org.tbk.electrum.example.shell.util.WalletTree;

import java.io.*;
import java.util.Optional;

@Slf4j
@Component
@CommandGroup(name = "Commands")
@RequiredArgsConstructor
class WalletTreeCommand {

    @NonNull
    private final ElectrumClient client;

    @Command(name = "wallet-tree", description = "write address to file")
    public void run(
            @Option(longName = "mnemonic", defaultValue = "", description = "mnemonic") String mnemonicArg,
            @Option(longName = "passphrase", defaultValue = "", description = "passphrase") String passphraseArg,
            @Option(longName = "amount", defaultValue = "21", description = "number of addresses per path") int amountArg,
            @Option(longName = "network", defaultValue = "mainnet", description = "mainnet|regtest|signet|testnet4") String networkArg,
            @Option(longName = "out", defaultValue = "", description = "write output to file") String outArg,
            CommandContext commandContext
    ) throws IOException, InterruptedException {
        String mnemonic = Optional.ofNullable(mnemonicArg).orElse("");
        String passphrase = Optional.ofNullable(passphraseArg).orElse("");
        int amount = amountArg > 0 ? amountArg : 21;
        Block network = MoreBlocks.toNetwork(networkArg);

        Wallet wallet = Wallet.from(network, Wallet.Mnemonic.builder()
                .mnemonic(mnemonic)
                .passphrase(passphrase)
                .build());

        try (Writer fileWriter = outArg.isEmpty() ? OutputStreamWriter.nullWriter() : new FileWriter(outArg);
             PrintWriter printWriter = new PrintWriter(new BufferedWriter(fileWriter))) {
            printWriter.printf("#### '%s'%n".formatted(mnemonic));
            printWriter.printf("##### passphrase: '%s'%n".formatted(passphrase));

            WalletTree.tree(wallet, amount).subscribe(it -> {
                String line = "%s;%s".formatted(it.getAddress(), it.getKeyPath());
                printWriter.printf("%s%n", line);
                printWriter.flush();

                commandContext.outputWriter().printf("%s%n", line);
                commandContext.outputWriter().flush();
            });
        }
    }
}