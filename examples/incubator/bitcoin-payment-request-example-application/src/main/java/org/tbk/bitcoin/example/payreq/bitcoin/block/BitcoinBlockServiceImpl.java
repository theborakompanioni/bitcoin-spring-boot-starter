package org.tbk.bitcoin.example.payreq.bitcoin.block;

import org.consensusj.bitcoin.json.pojo.BlockInfo;
import org.consensusj.bitcoin.jsonrpc.BitcoinClient;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jmolecules.ddd.annotation.Service;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;

import static org.tbk.bitcoin.example.payreq.bitcoin.block.BitcoinBlockSpecifications.byHash;

@Slf4j
@Service
@RequiredArgsConstructor
class BitcoinBlockServiceImpl implements BitcoinBlockService {

    @NonNull
    private final BitcoinClient bitcoinJsonRpcClient;

    @NonNull
    private final BitcoinBlocks blocks;

    @Override
    @Transactional
    public void createBlock(BlockInfo block) {
        BitcoinBlock bitcoinBlock = new BitcoinBlock(
                block.getHash(),
                block.getTime(),
                block.getNonce(),
                block.getConfirmations(),
                block.getSize(),
                block.getHeight(),
                block.getVersion(),
                block.getPreviousblockhash(),
                block.getNextblockhash());

        blocks.save(bitcoinBlock);
    }

    @Override
    @Transactional
    public void updatePreviousBlockIfPresent(BitcoinBlock.BitcoinBlockId parentBlockId) {
        BitcoinBlock parentBlock = blocks.findById(parentBlockId)
                .orElseThrow(() -> new EmptyResultDataAccessException(1));

        Optional<BitcoinBlock> previousBlockOrEmpty = blocks.findOne(byHash(parentBlock.getPreviousblockhash()));

        if (previousBlockOrEmpty.isEmpty()) {
            log.warn("cannot update child of block #{} {}: previous block {} is missing",
                    parentBlock.getHeight(), parentBlock.getHash(), parentBlock.getPreviousblockhash());
            return;
        }

        BitcoinBlock block = previousBlockOrEmpty.get();
        BlockInfo blockInfo = null;
        try {
            blockInfo = bitcoinJsonRpcClient.getBlockInfo(block.getHash());
        } catch (IOException e) {
            throw new RuntimeException("Error while fetching block via bitcoin api", e);
        }

        block.updateMutableValues(blockInfo);

        blocks.save(block);
    }
}
