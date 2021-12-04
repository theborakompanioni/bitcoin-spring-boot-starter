package org.tbk.bitcoin.bip85;

import fr.acinq.bitcoin.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

public final class DeterministicEntropy {

    private DeterministicEntropy() {
        throw new UnsupportedOperationException();
    }

    private static byte[] keyToEntropy(DeterministicWallet.ExtendedPrivateKey rootKey, KeyPath keyPath) {
        DeterministicWallet.ExtendedPrivateKey key = DeterministicWallet.derivePrivateKey(rootKey, keyPath);

        return Crypto.hmac512("bip-entropy-from-k".getBytes(StandardCharsets.UTF_8), key.getPrivateKey().value.toByteArray());
    }

    public static byte[] keyToEntropy(DeterministicWallet.ExtendedPrivateKey rootKey, long index) {
        checkArgument(index >= 0L, "Index must be positive");

        KeyPath keyPath = KeyPath.fromPath("m/83696968'/0'")
                .derive(DeterministicWallet.hardened(index));

        return keyToEntropy(rootKey, keyPath);
    }

    public static List<String> keyToMnemonic(DeterministicWallet.ExtendedPrivateKey rootKey, int wordCount, long index) {
        checkArgument(wordCount == 12 || wordCount == 18 || wordCount == 24, "Unsupported amount of words");
        checkArgument(index >= 0L, "Index must be positive");

        KeyPath keyPath = KeyPath.fromPath("m/83696968'/39'/0'")
                .derive(DeterministicWallet.hardened(wordCount))
                .derive(DeterministicWallet.hardened(index));

        byte[] entropy = keyToEntropy(rootKey, keyPath);
        int numBytes = ((wordCount - 1) * 11 >> 3) + 1;
        byte[] entropyRaw = Arrays.copyOfRange(entropy, 0, numBytes);

        return MnemonicCode.toMnemonics(entropyRaw);
    }

    public static DeterministicWallet.ExtendedPrivateKey keyToXprv(DeterministicWallet.ExtendedPrivateKey rootKey, long index) {
        checkArgument(index >= 0L, "Index must be positive");

        KeyPath keyPath = KeyPath.fromPath("m/83696968'/32'")
                .derive(DeterministicWallet.hardened(index));

        byte[] entropy = keyToEntropy(rootKey, keyPath);

        ByteVector32 chaincode = new ByteVector32(Arrays.copyOfRange(entropy, 0, 32));
        ByteVector32 secretKeyBytes = new ByteVector32(Arrays.copyOfRange(entropy, 32, 64));

        return new DeterministicWallet.ExtendedPrivateKey(secretKeyBytes, chaincode, 0, KeyPath.fromPath(""), 0L);
    }

    public static byte[] keyToHex(DeterministicWallet.ExtendedPrivateKey rootKey, int numBytes, long index) {
        checkArgument(numBytes >= 16 && numBytes <= 64, "numBytes must satisfy: 16 <= numBytes <= 64");
        checkArgument(index >= 0L, "Index must be positive");

        KeyPath keyPath = KeyPath.fromPath("m/83696968'/128169'")
                .derive(DeterministicWallet.hardened(numBytes))
                .derive(DeterministicWallet.hardened(index));

        byte[] entropy = keyToEntropy(rootKey, keyPath);

        return Arrays.copyOfRange(entropy, 0, numBytes);
    }
}
