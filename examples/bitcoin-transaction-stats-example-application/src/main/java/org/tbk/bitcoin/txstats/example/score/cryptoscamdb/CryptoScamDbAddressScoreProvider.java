package org.tbk.bitcoin.txstats.example.score.cryptoscamdb;

import com.google.common.collect.ImmutableMap;
import com.google.protobuf.Value;
import com.google.protobuf.util.Values;
import org.tbk.bitcoin.tool.cryptoscamdb.client.EntryDto;
import org.tbk.bitcoin.txstats.example.score.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class CryptoScamDbAddressScoreProvider implements AddressScoreProvider {
    private static final ProviderInfo contextProviderInfo = ProviderInfo.newBuilder()
            .setTitle("CryptoScamDb Database")
            .setVendor("cryptoscamdb.org")
            .setVersion("2020")
            .build();

    private static final ProviderInfo gradeProviderInfo = ProviderInfo.newBuilder()
            .setTitle(CryptoScamDbAddressScoreProvider.class.getSimpleName())
            .setVendor(Optional.ofNullable(CryptoScamDbAddressScoreProvider.class.getPackage().getImplementationVendor())
                    .orElse(CryptoScamDbAddressScoreProvider.class.getSimpleName()))
            .setVersion(Optional.ofNullable(CryptoScamDbAddressScoreProvider.class.getPackage().getImplementationVersion())
                    .orElse("0.0.0"))
            .build();

    private final CryptoScamDbService service;

    public CryptoScamDbAddressScoreProvider(CryptoScamDbService service) {
        this.service = requireNonNull(service);
    }

    @Override
    public List<AddressScoreAnalysis> gradeAddress(AddressScoreInput input) {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);


        List<EntryDto> metaInfoOfAddress = this.service.findMetaInfoOfAddress(input.getAddress().getAddress());

        AddressScoreContext addressScoreContext = AddressScoreContext.newBuilder()
                .setInput(input)
                .setProvider(contextProviderInfo)
                .build();

        int score = metaInfoOfAddress.isEmpty() ? 99 : 0;

        int confidence = (int) Math.min(metaInfoOfAddress.size(), 99L);

        ScoreValue scoreValue = ScoreValue.newBuilder()
                .setValue(score)
                .setConfidence(ScoreConfidence.newBuilder()
                        .setValue(confidence)
                        .putAdditionalData("algo", Values.of("min(count, 99)"))
                        .putAdditionalData("count", Values.of(metaInfoOfAddress.size()))
                        .putAdditionalData("min", Values.of(0))
                        .putAdditionalData("max", Values.of(99))
                        .build())
                .build();

        ImmutableMap<String, Value> additionalData = ImmutableMap.<String, Value>builder()
                .put("date", Values.of(now.toString()))
                .build();

        AddressScore addressScore = AddressScore.newBuilder()
                .setValue(scoreValue)
                .setProvider(gradeProviderInfo)
                .putAllAdditionalData(additionalData)
                .build();

        DebugMessage debugMessage = DebugMessage.newBuilder()
                .setDebugMessage("Just a test.")
                .build();

        AddressScoreAnalysis addressScoreAnalysis = AddressScoreAnalysis.newBuilder()
                .setName(this.getClass().getName())
                .setDescription("")
                .setProvider(gradeProviderInfo)
                .setScore(addressScore)
                .addContexts(addressScoreContext)
                .addDebugMessage(debugMessage)
                .build();

        return Collections.singletonList(addressScoreAnalysis);
    }
}
