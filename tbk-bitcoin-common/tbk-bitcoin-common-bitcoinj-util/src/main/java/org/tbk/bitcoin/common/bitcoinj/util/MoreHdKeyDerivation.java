package org.tbk.bitcoin.common.bitcoinj.util;

import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.crypto.HDPath;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public final class MoreHdKeyDerivation {

    private MoreHdKeyDerivation() {
        throw new UnsupportedOperationException();
    }

    public static DeterministicKey deriveChildKey(DeterministicKey parent, String path) {
        requireNonNull(parent, "'parent' must not be null");
        requireNonNull(path, "'path' must not be null");

        return deriveChildKey(parent, HDPath.parsePath(path));
    }

    public static DeterministicKey deriveChildKey(DeterministicKey parent, List<ChildNumber> childNumbers) {
        requireNonNull(parent, "'parent' must not be null");
        requireNonNull(childNumbers, "'childNumbers' must not be null");

        if (childNumbers.isEmpty()) {
            throw new IllegalStateException("Cannot derive child from empty list");
        }

        return deriveChildKeyRecursive(parent, childNumbers);
    }

    private static DeterministicKey deriveChildKeyRecursive(DeterministicKey parent, List<ChildNumber> childNumbers) {
        if (childNumbers.isEmpty()) {
            return parent;
        }

        ChildNumber first = childNumbers.get(0);
        DeterministicKey childKey = HDKeyDerivation.deriveChildKey(parent, first);

        List<ChildNumber> newChildNumbers = childNumbers.stream().skip(1).collect(Collectors.toList());

        return deriveChildKeyRecursive(childKey, newChildNumbers);
    }
}
