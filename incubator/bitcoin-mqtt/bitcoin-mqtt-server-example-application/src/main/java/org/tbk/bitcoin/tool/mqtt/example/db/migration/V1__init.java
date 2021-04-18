package org.tbk.bitcoin.tool.mqtt.example.db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;

@Component
public class V1__init extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        String sql = "create table if not exists moquette_user (created_at integer, username string, password_hash string)";

        try (PreparedStatement statement = context.getConnection().prepareStatement(sql)) {
            statement.execute();
        }
    }

}