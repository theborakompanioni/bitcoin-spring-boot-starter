package org.tbk.bitcoin.regtest.mining;

import com.google.common.collect.Lists;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Sha256Hash;
import org.consensusj.bitcoin.jsonrpc.BitcoinClient;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import static java.util.Objects.requireNonNull;

@Slf4j
public final class RegtestMinerImpl implements RegtestMiner {
    private static final Duration DEFAULT_SERVER_TIMEOUT = Duration.ofSeconds(10);

    private final BitcoinClient client;
    private final CoinbaseRewardAddressSupplier coinbaseRewardAddressSupplier;
    private final Duration serverTimeout;

    public RegtestMinerImpl(BitcoinClient client) {
        this(client, new RegtestEaterAddressSupplier());
    }

    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "class from external dependency")
    public RegtestMinerImpl(BitcoinClient client, CoinbaseRewardAddressSupplier coinbaseRewardAddressSupplier) {
        this(client, coinbaseRewardAddressSupplier, DEFAULT_SERVER_TIMEOUT);
    }

    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "class from external dependency")
    public RegtestMinerImpl(BitcoinClient client,
                            CoinbaseRewardAddressSupplier coinbaseRewardAddressSupplier,
                            Duration serverTimeout) {
        this.client = requireNonNull(client);
        this.coinbaseRewardAddressSupplier = requireNonNull(coinbaseRewardAddressSupplier);
        this.serverTimeout = requireNonNull(serverTimeout);
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

            this.client.waitForServer((int) serverTimeout.toSeconds());

            blockHashes.addAll(this.client.generateToAddress(count, coinbaseRewardAddress));
            while (blockHashes.size() < count) {
                // might have mined fewer blocks than requested, mine till requested amount is reached
                blockHashes.addAll(this.client.generateToAddress(1, coinbaseRewardAddress));
            }

            log.debug("Mined {} blocks with coinbase reward for address {}", blockHashes.size(), coinbaseRewardAddress);
            return blockHashes;
        } catch (IOException e) {
            throw new RuntimeException("Error while mining block", e);
        }
    }
}
