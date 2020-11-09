package org.tbk.bitcoin.txstats.example.score.label;

import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.NetworkParameters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.bitcoin.txstats.example.cache.AppCacheFacade;
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
                                                        AppCacheFacade caches) {
        var labelPredicate = new AddressReuseLabelPredicate(networkParameters, caches);
        return new PredicateScoreLabelProvider(labelPredicate);
    }

    @Bean
    public ScoreLabelProvider scriptTypesLabelProvider(AppCacheFacade caches) {
        var labelPredicate = new ScriptTypesLabelPredicate(caches);
        return new PredicateScoreLabelProvider(labelPredicate);
    }

    @Bean
    public ScoreLabelProvider roundFeeLabelPredicate(AppCacheFacade caches) {
        var labelPredicate = new RoundFeeLabelPredicate(caches);
        return new PredicateScoreLabelProvider(labelPredicate);
    }

    @Bean
    public ScoreLabelProvider sameAddressInInputsLabelProvider(NetworkParameters networkParameters,
                                                               AppCacheFacade caches) {
        var labelPredicate = new SameAddressInInputsLabelProvider(networkParameters, caches);
        return new PredicateScoreLabelProvider(labelPredicate);
    }

    @Bean
    public MinerLabelProvider minerLabelProvider(NetworkParameters networkParameters) {
        List<String> knownMinorPayoutAddresses = KnownPools.knownMinorPayoutAddresses();
        return new MinerLabelProvider(networkParameters, knownMinorPayoutAddresses);
    }
}
