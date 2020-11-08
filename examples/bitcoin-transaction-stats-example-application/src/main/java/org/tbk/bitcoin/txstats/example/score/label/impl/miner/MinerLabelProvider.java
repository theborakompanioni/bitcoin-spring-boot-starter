package org.tbk.bitcoin.txstats.example.score.label.impl.miner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutput;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.tbk.bitcoin.txstats.example.score.label.ScoreLabel;
import org.tbk.bitcoin.txstats.example.score.label.ScoreLabelProvider;
import org.tbk.bitcoin.txstats.example.util.MoreScripts;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A label provider indicating if a coinbase transactions comes from a
 * known or unknown miner.
 *
 * It makes use of the list provided by blockchain.com.
 * See https://github.com/blockchain/Blockchain-Known-Pools
 *
 */
@Slf4j
@AllArgsConstructor
public class MinerLabelProvider implements ScoreLabelProvider {

    private static final ScoreLabel knownMinerLabel = ScoreLabel.SimpleScoreLabel.of("known_miner");
    private static final ScoreLabel unknownMinerLabel = ScoreLabel.SimpleScoreLabel.of("unknown_miner");

    @NonNull
    private final NetworkParameters networkParameters;

    @NonNull
    private final List<String> knownMinorPayoutAddresses;

    @Override
    public Set<ScoreLabel> findLabels(Transaction tx) {
        if (!tx.isCoinBase()) {
            return Collections.emptySet();
        }

        boolean isKnown = isKnownMiner(tx);

        return isKnown ?
                Collections.singleton(knownMinerLabel) :
                Collections.singleton(unknownMinerLabel);
    }

    private boolean isKnownMiner(Transaction tx) {
        return tx.getOutputs().stream()
                .map(TransactionOutput::getScriptPubKey)
                .map(val -> MoreScripts.extractAddress(networkParameters, val))
                .flatMap(Optional::stream)
                .map(Object::toString)
                .anyMatch(knownMinorPayoutAddresses::contains);
    }
}
