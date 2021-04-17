package org.tbk.spring.testcontainer.bitcoind.regtest;

import com.google.common.collect.Lists;
import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Sha256Hash;

import java.io.IOException;
import java.util.List;

import static java.util.Objects.requireNonNull;

@Slf4j
public class BitcoindRegtestMinerImpl implements BitcoindRegtestMiner {

    private final BitcoinClient client;
    private final CoinbaseRewardAddressSupplier coinbaseRewardAddressSupplier;

    public BitcoindRegtestMinerImpl(BitcoinClient client) {
        this(client, new BitcoinClientCoinbaseRewardAddressSupplier(client));
    }

    public BitcoindRegtestMinerImpl(BitcoinClient client, CoinbaseRewardAddressSupplier coinbaseRewardAddressSupplier) {
        this.client = requireNonNull(client);
        this.coinbaseRewardAddressSupplier = requireNonNull(coinbaseRewardAddressSupplier);
    }

    @Override
    public List<Sha256Hash> mineBlocks(int count) {
        List<Sha256Hash> blockHashes = Lists.newArrayListWithCapacity(count);
        try {
            Address coinbaseRewardAddress = this.coinbaseRewardAddressSupplier.get();

            log.debug("Trying to mine {} block(s) with coinbase reward for address {}", count, coinbaseRewardAddress);

            while (blockHashes.size() < count) {
                blockHashes.addAll(this.client.generateToAddress(1, coinbaseRewardAddress));
            }

            log.debug("Mined {} blocks with coinbase reward for address {}", blockHashes.size(), coinbaseRewardAddress);
            return blockHashes;
        } catch (IOException e) {
            throw new RuntimeException("Error while mining block", e);
        }
    }
}
