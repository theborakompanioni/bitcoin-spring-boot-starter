package org.tbk.bitcoin.tool.mqtt.example.db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;

@Component
public class V2__add_user extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        // "6b88c087247aa2f07ee1c5956b8e1a9f4c7f892a70e324f1bb3d161e05ca107b" = sha256('bitcoin')
        String sql = "INSERT INTO moquette_user(created_at, username, password_hash) VALUES (1, 'user', '6b88c087247aa2f07ee1c5956b8e1a9f4c7f892a70e324f1bb3d161e05ca107b')";

        try (PreparedStatement statement = context.getConnection().prepareStatement(sql)) {
            statement.execute();
        }
    }

}