package org.tbk.electrum.example.shell.run;

import fr.acinq.bitcoin.Block;
import fr.acinq.bitcoin.KeyPath;
import org.tbk.electrum.example.shell.util.Wallet;
import reactor.core.publisher.Flux;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;

public class HdWalletTreeFileWriter {
    private static final String DEFAULT_MNEMONIC = "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about";

    private static final String DEFAULT_PASSPHRASE = "";

    private static final int DEFAULT_AMOUNT = 210;

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

        String fileName = "%s_%s.csv".formatted(mnemonic, passphrase.isEmpty() ? "_no_passphrase" : "_with_passphrase")
                .replace(" ", "_")
                .replace("-", "_");
        try (FileWriter fileWriter = new FileWriter(fileName);
             PrintWriter printWriter = new PrintWriter(fileWriter)) {
            printWriter.printf("#### '%s'%n".formatted(mnemonic));
            printWriter.printf("##### passphrase: '%s'%n".formatted(passphrase));

            addresses(wallet, amount).subscribe(it -> {
                String line = "%s;%s".formatted(it.getAddress(), it.getKeyPath());
                System.out.printf("%s%n", line);
                printWriter.printf("%s%n", line);
            });
        }
    }

    private static Flux<Wallet.AddressAndPath> addresses(Wallet wallet, int amount) {
        return Flux.merge(
                wallet.p2pkh(new KeyPath("")).take(amount),
                wallet.p2pkh(Wallet.deprecatedBip32P2pkhPath().derive(0)).take(amount),
                wallet.p2pkh(Wallet.deprecatedBip32P2pkhPath().derive(1)).take(amount),
                wallet.p2pkh(0, 0).take(amount),
                wallet.p2pkh(0, 1).take(amount),
                wallet.p2pkh(1, 0).take(amount),
                wallet.p2pkh(1, 1).take(amount),
                wallet.p2sh(0, 0).take(amount),
                wallet.p2sh(0, 1).take(amount),
                wallet.p2sh(1, 0).take(amount),
                wallet.p2sh(1, 1).take(amount),
                wallet.p2wpkh(0, 0).take(amount),
                wallet.p2wpkh(0, 1).take(amount),
                wallet.p2wpkh(1, 0).take(amount),
                wallet.p2wpkh(1, 1).take(amount)
        );
    }

}
