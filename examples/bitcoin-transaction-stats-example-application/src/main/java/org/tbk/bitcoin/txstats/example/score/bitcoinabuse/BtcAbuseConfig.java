package org.tbk.bitcoin.txstats.example.score.bitcoinabuse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;
import org.tbk.bitcoin.txstats.example.score.bitcoinabuse.client.BtcAbuseApiClient;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Supplier;

@Slf4j
@Configuration
public class BtcAbuseConfig implements InitializingBean {
    private static final String packagedForeverFileName = "/btcabuse/packaged-btc-abuse-forever-2020-1.csv";

    private static Optional<ClassPathResource> packagedForeverFile() {
        return Optional.of(new ClassPathResource(packagedForeverFileName))
                .filter(ClassPathResource::exists);

    }

    private static Path buildAbuseFileDestinationDirPathAndCreateIfNecessary() {
        Path source = Paths.get(BtcAbuseConfig.class.getResource("/").getPath());
        Path destinationDirPath = Paths.get(source.toAbsolutePath() + "/btcabuse/");

        if (!destinationDirPath.toFile().exists()) {
            try {
                Files.createDirectories(destinationDirPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return destinationDirPath;
    }

    private final Path fileDestinationDirPath;

    public BtcAbuseConfig() {
        this.fileDestinationDirPath = buildAbuseFileDestinationDirPathAndCreateIfNecessary();
    }

    @Bean
    @ConditionalOnBean(BtcAbuseApiClient.class)
    public BtcAbuseService btcAbuseService(BtcAbuseApiClient btcAbuseApiClient) {
        return new BtcAbuseServiceImpl(btcAbuseApiClient);
    }

    @Override
    public void afterPropertiesSet() {
        try {
            this.copyPackagedForeverFileIfPossible();
        } catch (IOException e) {
            log.error("Could not copy packaged btc abuse forever file", e);
        }
    }

    private void copyPackagedForeverFileIfPossible() throws IOException {
        Optional<ClassPathResource> packagedForeverFileOrEmpty = packagedForeverFile();

        if (!packagedForeverFileOrEmpty.isPresent()) {
            return;
        }

        ClassPathResource packagedForeverFile = packagedForeverFileOrEmpty.get();
        log.debug("Found packaged btc abuse forever file - will use this one for initialization: " +
                        "{}, last modified {}", packagedForeverFile.getFilename(),
                Instant.ofEpochMilli(packagedForeverFile.lastModified()));

        Path foreverFilePath = buildFileTargetPath(BtcAbuseApiClient.DownloadDuration.FOREVER).get();
        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(foreverFilePath.toFile()))) {
            try (InputStream is = packagedForeverFile.getInputStream()) {
                FileCopyUtils.copy(is, os);
            }
        }
    }

    private Supplier<Path> buildFileTargetPath(BtcAbuseApiClient.DownloadDuration duration) {
        return () -> {
            String postfix = buildFileTargetFileNamePostfix(duration);
            String fileName = String.format("btc-abuse-%s-%s.csv", duration.getDuration(), postfix);
            return Paths.get(fileDestinationDirPath.toAbsolutePath() + "/" + fileName);
        };
    }

    private String buildFileTargetFileNamePostfix(BtcAbuseApiClient.DownloadDuration duration) {
        LocalDate now = LocalDate.now();
        switch (duration) {
            case ONE_DAY:
                return now.toString();
            case THIRTY_DAYS:
                return now.getYear() + "-" + (now.getDayOfYear() / 7);
            case FOREVER:
                return now.getYear() + "-" + (now.getDayOfYear() / 90);
            default:
                throw new IllegalArgumentException("Cannot build file postfix for duration " + duration);
        }
    }
}
