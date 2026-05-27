package org.tbk.bitcoin.example.payreq.common;

import org.bitcoinj.base.BitcoinNetwork;
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
        return switch (this) {
            case mainnet -> MainNetParams.get();
            case testnet -> TestNet3Params.get();
            case regtest -> RegTestParams.get();
        };
    }

    public static Network fromNetworkParameters(NetworkParameters params) {
        if (BitcoinNetwork.ID_MAINNET.equals(params.getId())) {
            return Network.mainnet;
        }
        if (BitcoinNetwork.ID_TESTNET.equals(params.getId())) {
            return Network.testnet;
        }
        if (BitcoinNetwork.ID_REGTEST.equals(params.getId())) {
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
