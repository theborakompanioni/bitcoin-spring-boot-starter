package org.tbk.electrum.example.shell.run;

import fr.acinq.bitcoin.Block;
import org.tbk.electrum.example.shell.util.WalletTree;
import org.tbk.electrum.example.shell.util.Wallet;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;

public class WalletTreeFileWriter {
    private static final String DEFAULT_MNEMONIC = "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about";

    private static final String DEFAULT_PASSPHRASE = "";

    private static final int DEFAULT_AMOUNT = 21;

    public static void main(String[] args) throws IOException {
        String mnemonic = args.length == 0 ? DEFAULT_MNEMONIC : Optional.ofNullable(args[0])
                .orElse(DEFAULT_MNEMONIC);
        int amount = args.length <= 1 ? DEFAULT_AMOUNT : Optional.ofNullable(args[1])
                .map(Integer::parseUnsignedInt)
                .orElse(DEFAULT_AMOUNT);
        String passphrase = args.length <= 2 ? DEFAULT_PASSPHRASE : Optional.ofNullable(args[2])
                .orElse(DEFAULT_PASSPHRASE);

        Block network = Block.LivenetGenesisBlock;
        Wallet wallet = Wallet.from(network, Wallet.Mnemonic.builder()
                .mnemonic(mnemonic)
                .passphrase(passphrase)
                .build());

        String fileName = "wallet-tree_%s_%s-local.csv".formatted(mnemonic, passphrase.isEmpty() ? "_no_passphrase" : "_with_passphrase")
                .replace(" ", "_");
        try (FileWriter fileWriter = new FileWriter(fileName);
             PrintWriter printWriter = new PrintWriter(fileWriter)) {
            printWriter.printf("#### '%s'%n".formatted(mnemonic));
            printWriter.printf("##### passphrase: '%s'%n".formatted(passphrase));

            WalletTree.tree(wallet, amount).subscribe(it -> {
                String line = "%s;%s".formatted(it.getAddress(), it.getKeyPath());
                System.out.printf("%s%n", line);
                printWriter.printf("%s%n", line);
            });
        }
    }
}
