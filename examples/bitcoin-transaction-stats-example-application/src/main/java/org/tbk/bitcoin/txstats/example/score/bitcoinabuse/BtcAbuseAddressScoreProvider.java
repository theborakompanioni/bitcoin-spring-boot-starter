package org.tbk.bitcoin.txstats.example.score.bitcoinabuse;

import com.google.common.collect.ImmutableMap;
import com.google.protobuf.Value;
import com.google.protobuf.util.Values;
import org.tbk.bitcoin.tool.btcabuse.CheckResponseDto;
import org.tbk.bitcoin.txstats.example.score.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class BtcAbuseAddressScoreProvider implements AddressScoreProvider {
    private static final ProviderInfo contextProviderInfo = ProviderInfo.newBuilder()
            .setTitle("Bitcoin Abuse Database")
            .setVendor("bitcoinabuse.com")
            .setVersion("2020")
            .build();

    private static final ProviderInfo gradeProviderInfo = ProviderInfo.newBuilder()
            .setTitle(BtcAbuseAddressScoreProvider.class.getSimpleName())
            .setVendor(Optional.ofNullable(BtcAbuseAddressScoreProvider.class.getPackage().getImplementationVendor())
                    .orElse(BtcAbuseAddressScoreProvider.class.getSimpleName()))
            .setVersion(Optional.ofNullable(BtcAbuseAddressScoreProvider.class.getPackage().getImplementationVersion())
                    .orElse("0.0.0"))
            .build();


    private final BtcAbuseService service;

    public BtcAbuseAddressScoreProvider(BtcAbuseService service) {
        this.service = requireNonNull(service);
    }

    @Override
    public List<AddressScoreAnalysis> gradeAddress(AddressScoreInput input) {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);

        AddressScoreContext addressScoreContext = AddressScoreContext.newBuilder()
                .setInput(input)
                .setProvider(contextProviderInfo)
                .build();

        List<CheckResponseDto> metaInfoOfAddress = this.service.findMetaInfoOfAddress(input.getAddress().getAddress());

        long count = metaInfoOfAddress.stream()
                .mapToLong(CheckResponseDto::getCount)
                .sum();

        int score = count == 0L ? 99 :
                (count == 1 ? 50 :
                        (count < 5 ? 10 : 1));

        int confidence = (int) Math.min(count, 99L);

        ScoreValue scoreValue = ScoreValue.newBuilder()
                .setValue(score)
                .setConfidence(ScoreConfidence.newBuilder()
                        .setValue(confidence)
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
