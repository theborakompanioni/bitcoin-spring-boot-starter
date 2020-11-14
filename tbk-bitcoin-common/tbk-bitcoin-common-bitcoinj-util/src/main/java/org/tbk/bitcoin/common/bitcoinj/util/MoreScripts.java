package org.tbk.bitcoin.common.bitcoinj.util;

import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptException;
import org.bitcoinj.script.ScriptPattern;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public final class MoreScripts {

    private MoreScripts() {
        throw new UnsupportedOperationException();
    }

    public static List<Address> extractOutputAddress(Transaction tx) {
        if (tx == null) {
            return Collections.emptyList();
        }

        return tx.getOutputs().stream()
                .map(val -> MoreScripts.extractAddress(tx.getParams(), val.getScriptPubKey()))
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }

    public static Optional<Address> extractAddress(NetworkParameters networkParameters, Script script) {
        if (script == null) {
            return Optional.empty();
        }

        if (ScriptPattern.isOpReturn(script)) {
            return Optional.empty();
        }

        try {
            boolean forcePayToPubKey = true; // force public key to address - assume caller wants to avoid nulls
            return Optional.ofNullable(script.getToAddress(networkParameters, forcePayToPubKey));
        } catch (ScriptException se) {
            log.debug("Cannot extract address from script: {}: {}", script, se.getMessage());
            return Optional.empty();
        }
    }
}
