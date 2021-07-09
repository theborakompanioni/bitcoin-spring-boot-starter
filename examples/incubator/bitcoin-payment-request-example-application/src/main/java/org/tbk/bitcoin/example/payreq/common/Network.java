package org.tbk.bitcoin.example.payreq.common;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;

import java.util.Optional;

public enum Network {
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
            default:
                throw new IllegalArgumentException();
        }
    }

    public static Network fromNetworkParameters(NetworkParameters params) {
        if (NetworkParameters.ID_MAINNET.equals(params.getId())) {
            return Network.mainnet;
        }
        if (NetworkParameters.ID_TESTNET.equals(params.getId())) {
            return Network.testnet;
        }
        if (NetworkParameters.ID_REGTEST.equals(params.getId())) {
            return Network.regtest;
        }
        throw new IllegalArgumentException();
    }

    public static Optional<NetworkParameters> ofNullable(String value) {
        return Optional.ofNullable(value)
                .map(String::toLowerCase)
                .map(Network::valueOf)
                .map(Network::toNetworkParameters);
    }
}
