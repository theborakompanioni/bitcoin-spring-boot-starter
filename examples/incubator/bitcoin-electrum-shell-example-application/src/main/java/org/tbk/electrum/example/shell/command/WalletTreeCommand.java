package org.tbk.electrum.example.shell.command;

import fr.acinq.bitcoin.Block;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jline.terminal.Terminal;
import org.springframework.shell.standard.*;
import org.tbk.electrum.ElectrumClient;
import org.tbk.electrum.example.shell.util.WalletTree;
import org.tbk.electrum.example.shell.util.MoreBlocks;
import org.tbk.electrum.example.shell.util.Wallet;

import java.io.*;
import java.util.Optional;

@Slf4j
@ShellComponent
@ShellCommandGroup("Commands")
@RequiredArgsConstructor
class WalletTreeCommand extends AbstractShellComponent {

    @NonNull
    private final ElectrumClient client;

    @ShellMethod(key = "wallet-tree", value = "write address to file")
    public void run(
            @ShellOption(value = "mnemonic", defaultValue = "", help = "mnemonic") String mnemonicArg,
            @ShellOption(value = "passphrase", defaultValue = "", help = "passphrase") String passphraseArg,
            @ShellOption(value = "amount", defaultValue = "21", help = "number of addresses per path") int amountArg,
            @ShellOption(value = "network", defaultValue = "mainnet", help = "mainnet|regtest|signet|testnet4") String networkArg,
            @ShellOption(value = "out", defaultValue = "", help = "write output to file") String outArg
    ) throws IOException, InterruptedException {
        String mnemonic = Optional.ofNullable(mnemonicArg).orElse("");
        String passphrase = Optional.ofNullable(passphraseArg).orElse("");
        int amount = amountArg > 0 ? amountArg : 21;
        Block network = MoreBlocks.toNetwork(networkArg);

        Wallet wallet = Wallet.from(network, Wallet.Mnemonic.builder()
                .mnemonic(mnemonic)
                .passphrase(passphrase)
                .build());

        Terminal terminal = getTerminal();
        terminal.pause(true);

        try {
            try (Writer fileWriter = outArg.isEmpty() ? OutputStreamWriter.nullWriter() : new FileWriter(outArg);
                 PrintWriter printWriter = new PrintWriter(fileWriter)) {
                printWriter.printf("#### '%s'%n".formatted(mnemonic));
                printWriter.printf("##### passphrase: '%s'%n".formatted(passphrase));

                WalletTree.tree(wallet, amount).subscribe(it -> {
                    String line = "%s;%s".formatted(it.getAddress(), it.getKeyPath());
                    System.out.printf("%s%n", line);
                    printWriter.printf("%s%n", line);
                });
            }
        } finally {
            terminal.resume();
        }
    }
}