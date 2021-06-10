package org.tbk.bitcoin.example.payreq.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.api.migration.JavaMigration;
import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
public class DatabaseConfig {

    private static HikariConfig createHikariConfig(String database) {
        HikariConfig config = new HikariConfig();
        config.setPoolName("SQLitePool");
        config.setMinimumIdle(1);
        config.setMaximumPoolSize(3);
        config.setDriverClassName(org.sqlite.JDBC.class.getName());
        config.setJdbcUrl("jdbc:sqlite:" + database + ".db");
        config.setConnectionTestQuery("SELECT 1");
        config.setConnectionTimeout(TimeUnit.SECONDS.toMillis(30));
        config.setIdleTimeout(TimeUnit.SECONDS.toMillis(45));
        config.setMaxLifetime(TimeUnit.SECONDS.toMillis(60));
        return config;
    }

    @Bean
    public DataSource dataSource() {
        HikariConfig config = createHikariConfig("bitcoin_payment_request_example_application");
        return new HikariDataSource(config);
    }

    @Bean
    public FlywayConfigurationCustomizer flywayConfigurationCustomizer(ApplicationContext applicationContext) {
        return configuration -> {
            JavaMigration[] javaMigrations = applicationContext.getBeansOfType(JavaMigration.class)
                    .values().toArray(JavaMigration[]::new);

            configuration.javaMigrations(javaMigrations);
        };
    }

}
