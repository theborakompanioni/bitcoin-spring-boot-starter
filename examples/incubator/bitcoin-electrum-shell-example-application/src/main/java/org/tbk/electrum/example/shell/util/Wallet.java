package org.tbk.electrum.example.shell.util;

import fr.acinq.bitcoin.*;
import fr.acinq.bitcoin.DeterministicWallet.ExtendedPrivateKey;
import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import reactor.core.publisher.Flux;

import java.util.function.Function;

import static fr.acinq.bitcoin.DeterministicWallet.hardened;

@RequiredArgsConstructor
public class Wallet {

    public static KeyPath p2pkhPath(Block network) {
        long networkId = network.equals(Block.LivenetGenesisBlock) ? 0 : 1;
        return new KeyPath("")
                .derive(hardened(44))
                .derive(hardened(networkId))
                .derive(hardened(0));
    }

    public static KeyPath p2shPath(Block network) {
        long networkId = network.equals(Block.LivenetGenesisBlock) ? 0 : 1;
        return new KeyPath("")
                .derive(hardened(49))
                .derive(hardened(networkId))
                .derive(hardened(0));
    }

    public static KeyPath p2wpkhPath(Block network) {
        long networkId = network.equals(Block.LivenetGenesisBlock) ? 0 : 1;
        return new KeyPath("")
                .derive(hardened(84))
                .derive(hardened(networkId))
                .derive(hardened(0));
    }

    public static Wallet from(Block network, Mnemonic mnemonic) {
        byte[] seed = MnemonicCode.toSeed(mnemonic.getMnemonic(), mnemonic.getPassphrase());
        return new Wallet(network, DeterministicWallet.generate(seed));
    }

    @NonNull
    Block network;

    @NonNull
    ExtendedPrivateKey extendedPrivateKey;

    public Flux<KeyAndPath> keys(KeyPath keyPath) {
        return Flux.generate(() -> 0L, (index, sink) -> {
            sink.next(key(keyPath, index));
            return index + 1;
        });
    }

    public KeyAndPath key(KeyPath keyPath) {
        return KeyAndPath.builder()
                .keyPath(keyPath)
                .privateKey(extendedPrivateKey.derivePrivateKey(keyPath))
                .build();
    }

    public KeyAndPath key(KeyPath keyPath, long index) {
        return key(keyPath.derive(index));
    }

    public AddressAndPath deriveAddress(KeyPath keyPath, Function<PublicKey, String> mapper) {
        KeyAndPath key = key(keyPath);
        return AddressAndPath.builder()
                .keyPath(key.getKeyPath())
                .address(mapper.apply(key.getPrivateKey().getPublicKey()))
                .build();
    }

    public Flux<AddressAndPath> deriveAddresses(KeyPath keyPath, Function<PublicKey, String> mapper) {
        return keys(keyPath).map(it -> AddressAndPath.builder()
                .keyPath(it.getKeyPath())
                .address(mapper.apply(it.getPrivateKey().getPublicKey()))
                .build());
    }

    public Flux<AddressAndPath> p2pkh(KeyPath keyPath) {
        return deriveAddresses(keyPath, it -> it.p2pkhAddress(network.hash));
    }

    public Flux<AddressAndPath> p2pkh() {
        return p2pkh(p2pkhPath(network).derive(0));
    }

    public Flux<AddressAndPath> p2sh(KeyPath keyPath) {
        return deriveAddresses(keyPath, it -> it.p2shOfP2wpkhAddress(network.hash));
    }

    public Flux<AddressAndPath> p2sh() {
        return p2sh(p2shPath(network).derive(0));
    }

    public Flux<AddressAndPath> p2wpkh(KeyPath keyPath) {
        return deriveAddresses(keyPath, it -> it.p2wpkhAddress(network.hash));
    }

    public Flux<AddressAndPath> p2wpkh() {
        return p2wpkh(p2wpkhPath(network).derive(0));
    }

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

    @Value
    @Builder
    public static class AddressAndPath {
        @NonNull
        KeyPath keyPath;

        @NonNull
        String address;
    }

}
