package org.tbk.lightning.lnurl.example.db.migration;

import com.google.common.collect.Lists;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;

@Component
public class V1__init extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        String sql1 = """
                CREATE TABLE IF NOT EXISTS lnurl_auth_wallet_user (
                    id text PRIMARY KEY,
                    version integer,
                    created_at integer,
                    name text,
                    last_successful_auth_at integer,
                    account_disabled_at integer,
                    account_locked_at integer,
                    account_expired_at integer,
                    credentials_expired_at integer
                ) STRICT;
                """;
        String sql2 = """
                CREATE TABLE IF NOT EXISTS lnurl_auth_linking_key (
                    id text PRIMARY KEY,
                    version integer,
                    created_at integer,
                    lnurl_auth_wallet_user_id text,
                    linking_key text,
                    least_recently_used_k1 text,
                    FOREIGN KEY(lnurl_auth_wallet_user_id) REFERENCES lnurl_auth_wallet_user(id) ON DELETE CASCADE ON UPDATE CASCADE,
                    UNIQUE(linking_key)
                ) STRICT;
                """;
        String sql3 = """
                CREATE INDEX IF NOT EXISTS lnurl_auth_linking_key_fk1 ON lnurl_auth_linking_key (lnurl_auth_wallet_user_id);
                """;

        for (String sql : Lists.newArrayList(sql1, sql2, sql3)) {
            try (PreparedStatement statement = context.getConnection().prepareStatement(sql)) {
                statement.execute();
            }
        }
    }
}
