package org.tbk.bitcoin.btcabuse.sqlite;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.*;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Playground {

    private static HikariConfig createHikariConfig(String database) {
        HikariConfig config = new HikariConfig();
        config.setPoolName("TestSQLitePool");
        config.setMinimumIdle(1);
        config.setMaximumPoolSize(3);
        config.setDriverClassName(org.sqlite.JDBC.class.getName());
        config.setJdbcUrl("jdbc:sqlite:" + database + ".db");
        config.setConnectionTestQuery("SELECT 1");
        config.setMaxLifetime(TimeUnit.SECONDS.toMillis(60));
        config.setIdleTimeout(TimeUnit.SECONDS.toMillis(45));
        return config;
    }

    private DataSource dataSource;

    @BeforeEach
    public void setup() {
        String database = Playground.class.getSimpleName() + "-test";

        HikariConfig config = createHikariConfig(database);
        this.dataSource = new HikariDataSource(config);

        Flyway flyway = Flyway.configure()
                .dataSource(this.dataSource)
                .javaMigrations(new V1__init())
                .load();

        flyway.clean();
        flyway.migrate();
    }

    @Test
    public void test() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement statement = conn.createStatement()) {
            statement.setQueryTimeout(30);

            statement.executeUpdate("insert into btcabuse_check_response values(strftime('%s','now'), '12t9YDPgwueZ9NyMgw519p7AA8isjr6SMw', 2)");
            statement.executeUpdate("insert into btcabuse_check_response values(strftime('%s','now'), '1PaLmmeoe5Ktv613UGBCxCUZ27owv9Q6XY', 0)");

            ResultSet rs = statement.executeQuery("select * from btcabuse_check_response");
            while (rs.next()) {
                log.info("created_at := {}", rs.getInt("created_at"));
                log.info("address := {}", rs.getString("address"));
                log.info("count := {}", rs.getInt("count"));
            }
        }
    }

    public static class V1__init extends BaseJavaMigration {

        @Override
        public void migrate(Context context) throws Exception {
            String sql = "create table if not exists btcabuse_check_response (created_at integer, address string, count integer)";

            try (PreparedStatement statement = context.getConnection().prepareStatement(sql)) {
                statement.execute();
            }
        }
    }
}