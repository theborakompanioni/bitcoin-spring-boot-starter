package org.tbk.bitcoin.example.payreq.api.query;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;

import java.util.Optional;

public enum NetworkQueryParam {
    mainnet,
    testnet,
    regtest;

    public NetworkParameters toNetworkParameters() {
        switch (this) {
            case mainnet:
                return MainNetParams.get();
            case testnet:
                return TestNet3Params.get();
            case regtest:
                return RegTestParams.get();
        }
        throw new IllegalArgumentException();
    }

    public static NetworkQueryParam fromNetworkParameters(NetworkParameters params) {
        if (NetworkParameters.ID_MAINNET.equals(params.getId())) {
            return NetworkQueryParam.mainnet;
        }
        if (NetworkParameters.ID_TESTNET.equals(params.getId())) {
            return NetworkQueryParam.testnet;
        }
        if (NetworkParameters.ID_REGTEST.equals(params.getId())) {
            return NetworkQueryParam.regtest;
        }
        throw new IllegalArgumentException();
    }

    public static Optional<NetworkParameters> ofNullable(String value) {
        return Optional.ofNullable(value)
                .map(String::toLowerCase)
                .map(NetworkQueryParam::valueOf)
                .map(NetworkQueryParam::toNetworkParameters);
    }
}
