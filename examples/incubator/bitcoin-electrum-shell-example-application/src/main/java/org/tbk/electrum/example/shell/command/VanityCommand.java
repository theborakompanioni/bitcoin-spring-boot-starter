package org.tbk.electrum.example.shell.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableMap;
import fr.acinq.bitcoin.Block;
import fr.acinq.bitcoin.KeyPath;
import fr.acinq.bitcoin.MnemonicCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.tbk.electrum.example.shell.opt.AddressesFromMnemonic;
import org.tbk.electrum.example.shell.opt.AddressesFromMnemonic.KeyAndPath;
import org.tbk.electrum.example.shell.opt.AddressesFromMnemonic.Mnemonic;
import org.tbk.electrum.example.shell.opt.MoreRandom;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static java.util.Objects.requireNonNull;

@Slf4j
@ShellComponent
@ShellCommandGroup("Commands")
@RequiredArgsConstructor
class VanityCommand {

    @NonNull
    private final JsonMapper jsonMapper;

    @ShellMethod(key = "vanity", value = "vanity address")
    public String run(
            @ShellOption(value = "address-prefix", defaultValue = "", help = "address prefix") String addressPrefixArg,
            @ShellOption(value = "address-suffix", defaultValue = "", help = "address suffix") String addressSuffixArg,
            @ShellOption(value = "parallelism", defaultValue = "0", help = "parallelism level (default: # of processors / 2)") int parallelismArg,
            @ShellOption(value = "timeout", defaultValue = "-1", help = "timeout (e.g. 2s, 2d, default: -1 [no timeout])") String timeoutArg
    ) throws JsonProcessingException {
        int parallelism = parallelismArg > 0 ? parallelismArg : Math.max(1, Runtime.getRuntime().availableProcessors() / 2);
        String addressPrefix = addressPrefixArg == null ? "" : addressPrefixArg;
        String addressSuffix = addressSuffixArg == null ? "" : addressSuffixArg;
        Optional<Duration> timeout = parseTimeout(timeoutArg);

        if (!hasValidBech32Chars().test(addressPrefix)) {
            throw new IllegalArgumentException("address-prefix contains invalid bech32 chars");
        }
        if (!hasValidBech32Chars().test(addressSuffix)) {
            throw new IllegalArgumentException("address-suffix contains invalid bech32 chars");
        }

        Stopwatch stopwatch = Stopwatch.createStarted();

        Mono<Mnemonic> mnemonicMono = mineMnemonicMatching(parallelism, withAddressPrefixAndSuffix(addressPrefix, addressSuffix));
        Mnemonic mnemonic = requireNonNull(timeout.isEmpty() ? mnemonicMono.block() : mnemonicMono.block(timeout.get()));

        log.debug("vanity with address-prefix '{}' took {}", addressPrefix, stopwatch.stop());

        return jsonMapper.writeValueAsString(ImmutableMap.<String, String>builder()
                .put("mnemonic", mnemonic.getMnemonic())
                .put("passphrase", mnemonic.getPassphrase())
                .put("address", firstAddress(mnemonic, AddressesFromMnemonic.p2wpkhPath))
                .build());
    }

    private static Predicate<Mnemonic> withAddressPrefixAndSuffix(String prefix, String suffix) {
        Predicate<String> prefixPredicate = address -> address.startsWith("bc1q" + prefix);
        Predicate<String> suffixPredicate = address -> address.endsWith(suffix);
        return withAddressMatching(prefix.isEmpty() ? suffixPredicate : prefixPredicate.and(suffixPredicate));
    }

    private static String firstAddress(Mnemonic mnemonic, KeyPath keyPath) {
        KeyAndPath keyAndPath = AddressesFromMnemonic.key(mnemonic, keyPath, 0);
        return keyAndPath.getPrivateKey().getPublicKey().p2wpkhAddress(Block.LivenetGenesisBlock.hash);
    }

    private static Predicate<Mnemonic> withAddressMatching(Predicate<String> matcher) {
        return mnemonic -> {
            String address = firstAddress(mnemonic, AddressesFromMnemonic.p2wpkhPath);
            return matcher.test(address);
        };
    }

    private static Predicate<String> hasValidBech32Chars() {
        // from https://github.com/bitcoin/bips/blob/master/bip-0173.mediawiki
        return test -> !test.contains("1") &&
                       !test.contains("b") &&
                       !test.contains("i") &&
                       !test.contains("o");
    }

    private static Optional<Duration> parseTimeout(String timeoutArg) {
        Duration duration = DurationStyle.SIMPLE.parse(timeoutArg, ChronoUnit.SECONDS);
        return duration.isNegative() ? Optional.empty() : Optional.of(duration);
    }


    private Mono<Mnemonic> mineMnemonicMatching(int parallelism, Predicate<Mnemonic> predicate) {
        return requireNonNull(Mono.firstWithValue(IntStream.range(0, parallelism)
                .mapToObj(Integer::toString)
                .map(groupLabel -> Mono.fromCallable(() -> {
                            log.debug("Starting thread for group {}", groupLabel);
                            Mnemonic result = mineMnemonic(predicate);
                            log.debug("Found result in thread of group {}", groupLabel);
                            return result;
                        })
                        .subscribeOn(Schedulers.parallel())
                        .doOnCancel(() -> {
                            log.debug("Cancelled thread for group {}", groupLabel);
                        }))
                .toList()));
    }

    private Mnemonic mineMnemonic(Predicate<Mnemonic> predicate) {
        while (true) {
            if (Thread.currentThread().isInterrupted()) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(new InterruptedException("Thread interrupted: Abort operation 'mineAccount'."));
            }

            Mnemonic mnemonic = randomMnemonic();
            if (predicate.test(mnemonic)) {
                return mnemonic;
            }
        }
    }

    private Mnemonic randomMnemonic() {
        List<String> mnemonics = MnemonicCode.toMnemonics(MoreRandom.randomByteArray(128 / 8));
        return Mnemonic.builder()
                .mnemonic(String.join(" ", mnemonics))
                .build();
    }
}