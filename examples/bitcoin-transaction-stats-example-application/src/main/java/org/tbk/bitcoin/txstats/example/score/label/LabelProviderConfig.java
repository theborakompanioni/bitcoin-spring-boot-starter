package org.tbk.bitcoin.txstats.example.score.label;

import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.NetworkParameters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.bitcoin.txstats.example.cache.CacheFacade;
import org.tbk.bitcoin.txstats.example.score.label.impl.RoundFeeLabelPredicate;
import org.tbk.bitcoin.txstats.example.score.label.impl.ScriptTypesLabelPredicate;
import org.tbk.bitcoin.txstats.example.score.label.impl.miner.KnownPools;
import org.tbk.bitcoin.txstats.example.score.label.impl.miner.MinerLabelProvider;
import org.tbk.bitcoin.txstats.example.score.label.impl.reuse.AddressReuseLabelPredicate;
import org.tbk.bitcoin.txstats.example.score.label.impl.reuse.SameAddressInInputsLabelProvider;

import java.util.List;

@Slf4j
@Configuration
public class LabelProviderConfig {

    @Bean
    public ScoreLabelProvider addressReuseLabelProvider(NetworkParameters networkParameters,
                                                        CacheFacade caches) {
        var labelPredicate = new AddressReuseLabelPredicate(networkParameters, caches);
        return new PredicateScoreLabelProvider(labelPredicate);
    }

    @Bean
    public ScoreLabelProvider scriptTypesLabelProvider(NetworkParameters networkParameters,
                                                       CacheFacade caches) {
        var labelPredicate = new ScriptTypesLabelPredicate(networkParameters, caches);
        return new PredicateScoreLabelProvider(labelPredicate);
    }
    @Bean
    public ScoreLabelProvider roundFeeLabelPredicate(NetworkParameters networkParameters,
                                                       CacheFacade caches) {
        var labelPredicate = new RoundFeeLabelPredicate(networkParameters, caches);
        return new PredicateScoreLabelProvider(labelPredicate);
    }

    @Bean
    public ScoreLabelProvider sameAddressInInputsLabelProvider(NetworkParameters networkParameters,
                                                               CacheFacade caches) {
        var labelPredicate = new SameAddressInInputsLabelProvider(networkParameters, caches);
        return new PredicateScoreLabelProvider(labelPredicate);
    }

    @Bean
    public MinerLabelProvider minerLabelProvider(NetworkParameters networkParameters) {
        List<String> knownMinorPayoutAddresses = KnownPools.knownMinorPayoutAddresses();
        return new MinerLabelProvider(networkParameters, knownMinorPayoutAddresses);
    }
}
