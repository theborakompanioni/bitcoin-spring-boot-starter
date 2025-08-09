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

    private static int coinTypeForKeyPath(Block network) {
        return network.equals(Block.LivenetGenesisBlock) ? 0 : 1;
    }

    public static KeyPath p2pkhPath(Block network) {
        return new KeyPath("")
                .derive(hardened(44))
                .derive(hardened(coinTypeForKeyPath(network)));
    }

    public static KeyPath p2pkhPath(Block network, long account) {
        return p2pkhPath(network).derive(hardened(account));
    }

    public static KeyPath p2shPath(Block network) {
        return new KeyPath("")
                .derive(hardened(49))
                .derive(hardened(coinTypeForKeyPath(network)));
    }

    public static KeyPath p2shPath(Block network, long account) {
        return p2shPath(network).derive(hardened(account));
    }

    public static KeyPath p2wpkhPath(Block network) {
        return new KeyPath("")
                .derive(hardened(84))
                .derive(hardened(coinTypeForKeyPath(network)));
    }

    public static KeyPath p2wpkhPath(Block network, long account) {
        return p2wpkhPath(network).derive(hardened(account));
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

    public Flux<AddressAndPath> p2pkh() {
        return p2pkh(0, 0);
    }

    public Flux<AddressAndPath> p2pkh(long account, long change) {
        return p2pkh(p2pkhPath(network, account).derive(change));
    }

    public Flux<AddressAndPath> p2pkh(KeyPath keyPath) {
        return deriveAddresses(keyPath, it -> it.p2pkhAddress(network.hash));
    }

    public Flux<AddressAndPath> p2sh() {
        return p2sh(0, 0);
    }

    public Flux<AddressAndPath> p2sh(long account, long change) {
        return p2sh(p2shPath(network, account).derive(change));
    }

    public Flux<AddressAndPath> p2sh(KeyPath keyPath) {
        return deriveAddresses(keyPath, it -> it.p2shOfP2wpkhAddress(network.hash));
    }

    public Flux<AddressAndPath> p2wpkh() {
        return p2wpkh(0, 0);
    }

    public Flux<AddressAndPath> p2wpkh(long account, long change) {
        return p2wpkh(p2wpkhPath(network, account).derive(change));
    }

    public Flux<AddressAndPath> p2wpkh(KeyPath keyPath) {
        return deriveAddresses(keyPath, it -> it.p2wpkhAddress(network.hash));
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
