package org.tbk.bitcoin.example.payreq.db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;

@Component
public class V1__init extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        String sql = "create table if not exists invoice " +
                "(id string, version integer, created_at integer, valid_until integer, network string, comment string)";

        try (PreparedStatement statement = context.getConnection().prepareStatement(sql)) {
            statement.execute();
        }
    }

}