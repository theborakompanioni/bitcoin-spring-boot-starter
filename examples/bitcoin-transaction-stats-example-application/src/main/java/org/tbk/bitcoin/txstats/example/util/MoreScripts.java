package org.tbk.bitcoin.txstats.example.util;

import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptException;
import org.bitcoinj.script.ScriptPattern;

import java.util.Optional;

@Slf4j
public final class MoreScripts {

    private MoreScripts() {
        throw new UnsupportedOperationException();
    }

    public static Optional<Address> extractAddress(NetworkParameters networkParameters, Script script) {

        return Optional.ofNullable(script)
                .flatMap(val -> {
                    if (ScriptPattern.isOpReturn(script)) {
                        return Optional.empty();
                    }

                    try {
                        boolean forcePayToPubKey = true; // force public key to address - otherwise its null!
                        return Optional.ofNullable(val.getToAddress(networkParameters, forcePayToPubKey));
                    } catch (ScriptException se) {
                        log.debug("Cannot extract address from script: {}: {}", script, se.getMessage());
                        return Optional.empty();
                    }
                });
    }
}
