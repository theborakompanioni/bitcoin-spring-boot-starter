package org.tbk.electrum.example.shell.opt;

import fr.acinq.bitcoin.Block;
import fr.acinq.bitcoin.DeterministicWallet;
import fr.acinq.bitcoin.DeterministicWallet.ExtendedPrivateKey;
import fr.acinq.bitcoin.KeyPath;
import fr.acinq.bitcoin.MnemonicCode;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import reactor.core.publisher.Flux;

import java.util.Optional;

import static fr.acinq.bitcoin.DeterministicWallet.hardened;

public class AddressesFromMnemonic {
    private static final String DEFAULT_MNEMONIC = "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about";

    private static final int DEFAULT_AMOUNT = 21;

    // expected path: m/44'/0'/0'/0
    public static final KeyPath p2pkhPath = new KeyPath("")
            .derive(hardened(44))
            .derive(hardened(0))
            .derive(hardened(0));
    // expected path: m/49'/0'/0'/0
    public static final KeyPath p2shPath = new KeyPath("")
            .derive(hardened(49))
            .derive(hardened(0))
            .derive(hardened(0));
    // expected path: m/84'/0'/0'/0
    public static final KeyPath p2wpkhPath = new KeyPath("")
            .derive(hardened(84))
            .derive(hardened(0))
            .derive(hardened(0));

    public static void main(String[] args) {
        String mnemonic = args.length == 0 ? DEFAULT_MNEMONIC : Optional.ofNullable(args[0])
                .orElse(DEFAULT_MNEMONIC);
        int amount = args.length <= 1 ? DEFAULT_AMOUNT : Optional.ofNullable(args[1])
                .map(Integer::parseUnsignedInt)
                .orElse(DEFAULT_AMOUNT);

        byte[] seed = MnemonicCode.toSeed(mnemonic, "");
        ExtendedPrivateKey extendedPrivateKey = DeterministicWallet.generate(seed);

        p2pkh(extendedPrivateKey, amount);
        p2sh(extendedPrivateKey, amount);
        p2wpkh(extendedPrivateKey, amount);
    }

    public static Flux<KeyAndPath> keys(Mnemonic mnemonic, KeyPath keyPath) {
        return keys(extendedPrivateKey(mnemonic), keyPath);
    }

    public static ExtendedPrivateKey extendedPrivateKey(Mnemonic mnemonic) {
        byte[] seed = MnemonicCode.toSeed(mnemonic.getMnemonic(), mnemonic.getPassphrase());
        return DeterministicWallet.generate(seed);
    }

    public static Flux<KeyAndPath> keys(ExtendedPrivateKey extendedPrivateKey, KeyPath keyPath) {
        return Flux.generate(() -> 0L, (index, sink) -> {
            sink.next(key(extendedPrivateKey, keyPath, index));
            return index + 1;
        });
    }

    public static KeyAndPath key(Mnemonic mnemonic, KeyPath keyPath, long index) {
        ExtendedPrivateKey extendedPrivateKey = extendedPrivateKey(mnemonic);
        KeyPath derivedPath = keyPath.derive(index);
        return KeyAndPath.builder()
                .keyPath(derivedPath)
                .privateKey(extendedPrivateKey.derivePrivateKey(keyPath))
                .build();
    }

    public static KeyAndPath key(ExtendedPrivateKey extendedPrivateKey, KeyPath keyPath, long index) {
        KeyPath derivedPath = keyPath.derive(index);
        return KeyAndPath.builder()
                .keyPath(derivedPath)
                .privateKey(extendedPrivateKey.derivePrivateKey(keyPath))
                .build();
    }

    @Value
    @Builder
    public static class KeyAndPath {
        @NonNull
        ExtendedPrivateKey privateKey;
        @NonNull
        KeyPath keyPath;
    }

    public static void p2pkh(ExtendedPrivateKey extendedPrivateKey, int amount) {
        KeyPath main = p2pkhPath.derive(0);
        keys(extendedPrivateKey, main)
                .repeat(amount)
                .subscribe(it -> {
                    String address = it.getPrivateKey().getPublicKey().p2pkhAddress(Block.LivenetGenesisBlock.hash);
                    System.out.printf("%s;%s%n", it.getKeyPath(), address);
                });
    }

    public static void p2sh(ExtendedPrivateKey extendedPrivateKey, int amount) {
        KeyPath main = p2shPath.derive(0);

        keys(extendedPrivateKey, main)
                .repeat(amount)
                .subscribe(it -> {
                    String address = it.getPrivateKey().getPublicKey().p2shOfP2wpkhAddress(Block.LivenetGenesisBlock.hash);
                    System.out.printf("%s;%s%n", it.getKeyPath(), address);
                });
    }

    public static void p2wpkh(ExtendedPrivateKey extendedPrivateKey, int amount) {
        KeyPath main = p2wpkhPath.derive(0);

        keys(extendedPrivateKey, main)
                .repeat(amount)
                .subscribe(it -> {
                    String address = it.getPrivateKey().getPublicKey().p2wpkhAddress(Block.LivenetGenesisBlock.hash);
                    System.out.printf("%s;%s%n", it.getKeyPath(), address);
                });
    }

    @Value
    @Builder
    public static class Mnemonic {
        @NonNull
        String mnemonic;

        @Builder.Default
        String passphrase = "";
    }
}
