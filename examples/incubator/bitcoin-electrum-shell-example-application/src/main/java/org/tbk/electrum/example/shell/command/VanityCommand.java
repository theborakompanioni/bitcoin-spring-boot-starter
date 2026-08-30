package org.tbk.electrum.example.shell.command;

import com.google.common.base.CharMatcher;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableMap;
import fr.acinq.bitcoin.Block;
import fr.acinq.bitcoin.KeyPath;
import fr.acinq.bitcoin.MnemonicCode;
import fr.acinq.bitcoin.PublicKey;
import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.CommandGroup;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;
import org.tbk.electrum.example.shell.util.MoreBlocks;
import org.tbk.electrum.example.shell.util.MoreRandom;
import org.tbk.electrum.example.shell.util.Wallet;
import org.tbk.electrum.example.shell.util.Wallet.Mnemonic;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import javax.annotation.Nullable;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static java.util.Objects.requireNonNull;

@Slf4j
@Component
@CommandGroup(name = "Commands")
@RequiredArgsConstructor
class VanityCommand {

    @NonNull
    private final JsonMapper jsonMapper;

    @Command(name = "vanity", description = "generate a vanity address")
    public String run(
            @Option(longName = "address-prefix", defaultValue = "", description = "address prefix") String addressPrefixArg,
            @Option(longName = "address-suffix", defaultValue = "", description = "address suffix") String addressSuffixArg,
            @Option(longName = "network", defaultValue = "mainnet", description = "mainnet|regtest|signet|testnet4") String networkArg,
            @Option(longName = "address-type", defaultValue = "p2wpkh", description = "p2tr|p2wpkh|p2wpkh_p2sh|p2pkh") String addressTypeArg,
            @Option(longName = "path", defaultValue = "", description = "m/86'/0'/0'/0|m/84'/0'/0'/0|m/49'/0'/0'/0|m/44'/0'/0'/0") String pathArg,
            @Option(longName = "parallelism", defaultValue = "0", description = "parallelism level (default: # of processors / 2)") int parallelismArg,
            @Option(longName = "timeout", defaultValue = "-1", description = "timeout (e.g. 2s, 2d, default: -1 [no timeout])") String timeoutArg
    ) throws JacksonException {
        int parallelism = parallelismArg > 0 ? parallelismArg : Math.max(1, Runtime.getRuntime().availableProcessors() / 2);
        String addressPrefix = addressPrefixArg == null ? "" : addressPrefixArg;
        String addressSuffix = addressSuffixArg == null ? "" : addressSuffixArg;
        Optional<Duration> timeout = parseTimeout(timeoutArg);
        Optional<KeyPath> keyPath = Optional.ofNullable(pathArg)
                .filter(it -> !it.isBlank())
                .map(KeyPath::fromPath);
        VanityCommandParams.AddressType addressType = VanityCommandParams.toAddressType(addressTypeArg);

        if (!addressType.validChars().test(addressPrefix)) {
            throw new IllegalArgumentException("address-prefix contains invalid bech32 chars");
        }
        if (!addressType.validChars().test(addressSuffix)) {
            throw new IllegalArgumentException("address-suffix contains invalid bech32 chars");
        }

        VanityCommandParams params = VanityCommandParams.builder()
                .network(MoreBlocks.toNetwork(networkArg))
                .addressType(addressType)
                .keyPath(keyPath.orElse(null))
                .prefix(addressPrefix)
                .suffix(addressSuffix)
                .build();

        Predicate<Mnemonic> mnemonicPredicate = mnemonic -> toFirstAddressMapper(params)
                .andThen(it -> params.getAddressPredicate().test(it.getAddress()))
                .apply(mnemonic);

        Stopwatch stopwatch = Stopwatch.createStarted();

        Mono<Mnemonic> mnemonicMono = mineMnemonicMatching(parallelism, mnemonicPredicate);
        Mnemonic mnemonic = requireNonNull(timeout.isEmpty() ? mnemonicMono.block() : mnemonicMono.block(timeout.get()));

        log.debug("vanity with address-prefix '{}' took {}", addressPrefix, stopwatch.stop());

        Wallet.AddressAndPath firstAddress = toFirstAddressMapper(params, mnemonic);
        return jsonMapper.writeValueAsString(ImmutableMap.<String, String>builder()
                .put("mnemonic", mnemonic.getMnemonic())
                .put("passphrase", mnemonic.getPassphrase())
                .put("path", firstAddress.getKeyPath().toString())
                .put("address", firstAddress.getAddress())
                .build());
    }

    private static Wallet.AddressAndPath toFirstAddressMapper(VanityCommandParams params, Mnemonic mnemonic) {
        return toFirstAddressMapper(params).apply(mnemonic);
    }

    private static Function<Mnemonic, Wallet.AddressAndPath> toFirstAddressMapper(VanityCommandParams params) {
        return mnemonic -> Wallet.from(params.getNetwork(), mnemonic)
                .deriveAddress(params.getKeyPath().derive(0), params.getAddressMapper());
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

    private Wallet.Mnemonic randomMnemonic() {
        List<String> mnemonics = MnemonicCode.toMnemonics(MoreRandom.randomByteArray(128 / 8));
        return Wallet.Mnemonic.builder()
                .mnemonic(String.join(" ", mnemonics))
                .build();
    }

    @Value
    @Builder
    public static class VanityCommandParams {
        @Builder.Default
        String prefix = "";

        @Builder.Default
        String suffix = "";

        @NonNull
        Block network;

        @NonNull
        AddressType addressType;

        @Nullable
        KeyPath keyPath;

        public KeyPath getKeyPath() {
            return keyPath != null ? keyPath : addressType.standardKeyPath(network);
        }

        public Function<PublicKey, String> getAddressMapper() {
            return addressType.addressMapper(network);
        }

        public Predicate<String> getAddressPredicate() {
            return addressType.addressPrefix(network).stream()
                    .map(addressPrefix -> withAddressPrefixAndSuffix(addressPrefix + prefix, suffix))
                    .reduce(Predicate::or)
                    .orElse(s -> false);
        }

        private static Predicate<String> withAddressPrefixAndSuffix(String prefix, String suffix) {
            Predicate<String> prefixPredicate = address -> address.startsWith(prefix);
            Predicate<String> suffixPredicate = address -> address.endsWith(suffix);
            return prefix.isEmpty() ? suffixPredicate : prefixPredicate.and(suffixPredicate);
        }

        public static AddressType toAddressType(String addressType) {
            return addressType.isBlank() ? AddressType.p2wpkh : AddressType.valueOf(addressType);
        }

        public enum AddressType {
            p2tr, p2wpkh, p2wpkh_p2sh, p2pkh;

            public List<String> addressPrefix(Block network) {
                return switch (network) {
                    case Block b when b.equals(Block.LivenetGenesisBlock) -> switch (this) {
                        case p2pkh -> List.of("1");
                        case p2wpkh_p2sh -> List.of("3");
                        case p2wpkh -> List.of("bc1q");
                        case p2tr -> List.of("bc1p");
                    };
                    case Block b when b.equals(Block.RegtestGenesisBlock) -> switch (this) {
                        case p2pkh -> List.of("m", "n");
                        case p2wpkh_p2sh -> List.of("2");
                        case p2wpkh -> List.of("bcrt1q");
                        case p2tr -> List.of("bcrt1p");
                    };
                    // signet, testnet, testnet4
                    default -> switch (this) {
                        case p2pkh -> List.of("m", "n");
                        case p2wpkh_p2sh -> List.of("2");
                        case p2wpkh -> List.of("tb1q");
                        case p2tr -> List.of("tb1p");
                    };
                };
            }

            public Function<PublicKey, String> addressMapper(Block network) {
                return it -> switch (this) {
                    case p2pkh -> it.p2pkhAddress(network.hash);
                    case p2wpkh_p2sh -> it.p2shOfP2wpkhAddress(network.hash);
                    case p2wpkh -> it.p2wpkhAddress(network.hash);
                    case p2tr -> it.p2trAddress(network.hash);
                };
            }

            public KeyPath standardKeyPath(Block network) {
                return switch (this) {
                    case p2pkh -> Wallet.bip44P2pkhPath(network, 0).derive(0);
                    case p2wpkh_p2sh -> Wallet.bip49P2shPath(network, 0).derive(0);
                    case p2wpkh -> Wallet.bip84P2wpkhPath(network, 0).derive(0);
                    case p2tr -> Wallet.bip86P2trPath(network, 0).derive(0);
                };
            }

            public Predicate<String> validChars() {
                return switch (this) {
                    case p2pkh, p2wpkh_p2sh -> hasValidBase58Chars;
                    case p2wpkh, p2tr -> hasValidBech32Chars;
                };
            }

            // from https://github.com/bitcoin/bips/blob/master/bip-0173.mediawiki
            private static final String bech32Chars = "qpzry9x8gf2tvdw0s3jn54khce6mua7l";
            private static final CharMatcher bech32CharMatcher = CharMatcher.anyOf(bech32Chars);
            private static final Predicate<String> hasValidBech32Chars = bech32CharMatcher::matchesAllOf;

            private static final String base58Chars = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
            private static final CharMatcher base58CharMatcher = CharMatcher.anyOf(base58Chars);
            private static final Predicate<String> hasValidBase58Chars = base58CharMatcher::matchesAllOf;
        }
    }
}