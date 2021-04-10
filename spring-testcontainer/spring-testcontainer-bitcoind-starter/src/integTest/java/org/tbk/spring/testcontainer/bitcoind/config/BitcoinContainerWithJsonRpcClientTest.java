package org.tbk.spring.testcontainer.bitcoind.config;

import com.msgilligan.bitcoinj.json.pojo.BlockChainInfo;
import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.params.RegTestParams;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class BitcoinContainerWithJsonRpcClientTest {

    @SpringBootApplication
    public static class BitcoinContainerClientTestApplication {

        public static void main(String[] args) {
            new SpringApplicationBuilder()
                    .sources(BitcoinContainerClientTestApplication.class)
                    .web(WebApplicationType.NONE)
                    .run(args);
        }
    }

    @Autowired
    private BitcoinClient bitcoinJsonRpcClient;

    @Test
    public void testGetBlockChainInfo() throws IOException {
        BlockChainInfo blockChainInfo = bitcoinJsonRpcClient.getBlockChainInfo();
        assertThat(blockChainInfo.getChain(), is("regtest"));
    }

    @Test
    public void testGenerateToAddress() throws IOException {
        BlockChainInfo blockChainInfoBefore = bitcoinJsonRpcClient.getBlockChainInfo();
        int blocksAmountBefore = blockChainInfoBefore.getBlocks();

        Address newAddress = bitcoinJsonRpcClient.getNewAddress();

        List<Sha256Hash> initialMinedBlockHashes = bitcoinJsonRpcClient.generateToAddress(1, newAddress);
        assertThat(initialMinedBlockHashes, hasSize(1));

        Sha256Hash initialMinedBlockHash = initialMinedBlockHashes.get(0);

        BlockChainInfo blockChainInfoAfter = bitcoinJsonRpcClient.getBlockChainInfo();

        int blocksAmountAfter = blockChainInfoAfter.getBlocks();
        assertThat(blocksAmountAfter, is(blocksAmountBefore + 1));

        assertThat(blockChainInfoAfter.getBestBlockHash(), is(initialMinedBlockHash));
    }

    @Test
    public void testGenerateToAddressExpectingCoins() throws IOException {
        Address newAddress = bitcoinJsonRpcClient.getNewAddress();
        // an address not controlled by the bitcoin core testcontainer (taken from second_wallet in electrum module)
        Address regtestEaterAddress = Address.fromString(RegTestParams.get(), "bcrt1q4m4fds2rdtgde67ws5aema2a2wqvv7uzyxqc4j");

        Coin balanceBefore = bitcoinJsonRpcClient.getBalance();
        assertThat(balanceBefore, is(Coin.ZERO));

        List<Sha256Hash> initialMinedBlockHashes = bitcoinJsonRpcClient.generateToAddress(1, newAddress);
        assertThat("an additional block has been mined", initialMinedBlockHashes, hasSize(1));

        // mine a 100 blocks in order for the coinbase transaction to be spendable
        long counter = 0L;
        while (counter < 100L) {
            List<Sha256Hash> newlyMinedBlocks = bitcoinJsonRpcClient.generateToAddress(1, regtestEaterAddress);
            counter += newlyMinedBlocks.size();
        }

        // immediately after the block is mined, the rpc client sometimes
        // reports the balance as zero for a short amount of time..
        // solution: poll every 10ms for 10s as a short workaround
        Coin balanceAfter = Flux.interval(Duration.ofMillis(10))
                .map(foo -> {
                    try {
                        return bitcoinJsonRpcClient.getBalance();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .filter(val -> !val.isZero())
                .blockFirst(Duration.ofSeconds(10));

        assertThat("block reward is spendable after blocks are mined", balanceAfter, is(Coin.FIFTY_COINS));
    }
}
