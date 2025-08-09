package org.tbk.electrum.example.shell.run;

import fr.acinq.bitcoin.Block;
import org.tbk.electrum.example.shell.util.Wallet;

import java.util.Optional;

public class AddressesFromMnemonic {
    private static final String DEFAULT_MNEMONIC = "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about";

    private static final int DEFAULT_AMOUNT = 21;

    public static void main(String[] args) {
        String mnemonic = args.length == 0 ? DEFAULT_MNEMONIC : Optional.ofNullable(args[0])
                .orElse(DEFAULT_MNEMONIC);
        int amount = args.length <= 1 ? DEFAULT_AMOUNT : Optional.ofNullable(args[1])
                .map(Integer::parseUnsignedInt)
                .orElse(DEFAULT_AMOUNT);

        Wallet wallet = Wallet.from(Block.LivenetGenesisBlock, Wallet.Mnemonic.builder()
                .mnemonic(mnemonic)
                .passphrase("")
                .build());

        wallet.p2pkh()
                .take(amount)
                .subscribe(it -> {
                    System.out.printf("%s;%s%n", it.getKeyPath(), it.getAddress());
                });
        wallet.p2sh()
                .take(amount)
                .subscribe(it -> {
                    System.out.printf("%s;%s%n", it.getKeyPath(), it.getAddress());
                });
        wallet.p2wpkh()
                .take(amount)
                .subscribe(it -> {
                    System.out.printf("%s;%s%n", it.getKeyPath(), it.getAddress());
                });
    }
}
