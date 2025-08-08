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
import java.util.function.Function;

import static fr.acinq.bitcoin.DeterministicWallet.hardened;

public class AddressesFromMnemonic {
    private static final String DEFAULT_MNEMONIC = "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about";

    private static final int DEFAULT_AMOUNT = 21;

    // expected path: m/44'/0'/0'
    public static final Function<Block, KeyPath> p2pkhPath = network -> {
        long networkId = network.equals(Block.LivenetGenesisBlock) ? 0 : 1;
        return new KeyPath("")
                .derive(hardened(44))
                .derive(hardened(networkId))
                .derive(hardened(0));
    };

    // expected path: m/49'/0'/0'
    public static final Function<Block, KeyPath> p2shPath = network -> {
        long networkId = network.equals(Block.LivenetGenesisBlock) ? 0 : 1;
        return new KeyPath("")
                .derive(hardened(49))
                .derive(hardened(networkId))
                .derive(hardened(0));
    };
    // expected path: m/84'/0'/0'
    public static final Function<Block, KeyPath> p2wpkhPath = network -> {
        long networkId = network.equals(Block.LivenetGenesisBlock) ? 0 : 1;
        return new KeyPath("")
                .derive(hardened(84))
                .derive(hardened(networkId))
                .derive(hardened(0));
    };

    @Value
    @Builder
    public static class Mnemonic {
        @NonNull
        String mnemonic;

        @Builder.Default
        String passphrase = "";
    }

    @Value
    @Builder
    public static class KeyAndPath {
        @NonNull
        ExtendedPrivateKey privateKey;
        @NonNull
        KeyPath keyPath;
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

    public static void p2pkh(Block network, ExtendedPrivateKey extendedPrivateKey, int amount) {
        keys(extendedPrivateKey, p2pkhPath.apply(network).derive(0))
                .repeat(amount)
                .subscribe(it -> {
                    String address = it.getPrivateKey().getPublicKey().p2pkhAddress(network.hash);
                    System.out.printf("%s;%s%n", it.getKeyPath(), address);
                });
    }

    public static void p2sh(Block network, ExtendedPrivateKey extendedPrivateKey, int amount) {
        keys(extendedPrivateKey, p2shPath.apply(network).derive(0))
                .repeat(amount)
                .subscribe(it -> {
                    String address = it.getPrivateKey().getPublicKey().p2shOfP2wpkhAddress(network.hash);
                    System.out.printf("%s;%s%n", it.getKeyPath(), address);
                });
    }

    public static void p2wpkh(Block network, ExtendedPrivateKey extendedPrivateKey, int amount) {
        keys(extendedPrivateKey, p2wpkhPath.apply(network).derive(0)).repeat(amount)
                .subscribe(it -> {
                    String address = it.getPrivateKey().getPublicKey().p2wpkhAddress(network.hash);
                    System.out.printf("%s;%s%n", it.getKeyPath(), address);
                });
    }

    public static void main(String[] args) {
        String mnemonic = args.length == 0 ? DEFAULT_MNEMONIC : Optional.ofNullable(args[0])
                .orElse(DEFAULT_MNEMONIC);
        int amount = args.length <= 1 ? DEFAULT_AMOUNT : Optional.ofNullable(args[1])
                .map(Integer::parseUnsignedInt)
                .orElse(DEFAULT_AMOUNT);

        byte[] seed = MnemonicCode.toSeed(mnemonic, "");
        ExtendedPrivateKey extendedPrivateKey = DeterministicWallet.generate(seed);

        p2pkh(Block.LivenetGenesisBlock, extendedPrivateKey, amount);
        p2sh(Block.LivenetGenesisBlock, extendedPrivateKey, amount);
        p2wpkh(Block.LivenetGenesisBlock, extendedPrivateKey, amount);
    }
}
