package org.tbk.bitcoin.regtest.mining;

import com.google.common.collect.Lists;
import org.consensusj.bitcoin.jsonrpc.BitcoinClient;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Sha256Hash;

import java.io.IOException;
import java.util.List;

import static java.util.Objects.requireNonNull;

@Slf4j
public final class RegtestMinerImpl implements RegtestMiner {

    private final BitcoinClient client;
    private final CoinbaseRewardAddressSupplier coinbaseRewardAddressSupplier;

    public RegtestMinerImpl(BitcoinClient client) {
        this(client, new RegtestEaterAddressSupplier());
    }

    public RegtestMinerImpl(BitcoinClient client, CoinbaseRewardAddressSupplier coinbaseRewardAddressSupplier) {
        this.client = requireNonNull(client);
        this.coinbaseRewardAddressSupplier = requireNonNull(coinbaseRewardAddressSupplier);
    }

    @Override
    public List<Sha256Hash> mineBlocks(int count) {
        return this.mineBlocks(count, this.coinbaseRewardAddressSupplier);
    }

    @Override
    public List<Sha256Hash> mineBlocks(int count, CoinbaseRewardAddressSupplier addressSupplier) {
        List<Sha256Hash> blockHashes = Lists.newArrayListWithCapacity(count);
        try {
            Address coinbaseRewardAddress = addressSupplier.get();

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
