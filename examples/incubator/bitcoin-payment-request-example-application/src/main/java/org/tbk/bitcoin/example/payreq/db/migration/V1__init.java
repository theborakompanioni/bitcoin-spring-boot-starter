package org.tbk.bitcoin.example.payreq.db.migration;

import com.google.common.collect.Lists;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;

@Component
public class V1__init extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {

        String sql1 = "create table if not exists customer_order " +
                "(id string PRIMARY KEY, version integer, created_at integer, status string)";

        String sql2 = "create table if not exists customer_order_line_item " +
                "(id string PRIMARY KEY, order_id string, position integer, name string, price string, currency_unit string, display_price string, quantity integer, " +
                "FOREIGN KEY(order_id) REFERENCES customer_order(id)" +
                ")";

        String sql3 = "create table if not exists payment_request " +
                "(id string PRIMARY KEY, version integer, created_at integer, order_id string, dtype string, valid_until integer, network string, address string, amount string, currency_unit string, display_price string, " +
                "FOREIGN KEY(order_id) REFERENCES customer_order(id)" +
                ")";

        String sql4 = "create table if not exists invoice " +
                "(id string PRIMARY KEY, version integer, created_at integer, order_id string, comment string," +
                "FOREIGN KEY(order_id) REFERENCES customer_order(id)" +
                ")";

        String sql5 = "create table if not exists donation " +
                "(id string PRIMARY KEY, version integer, created_at integer, order_id string, payment_request_id string, display_price string, description string, payment_url string, comment string, " +
                "FOREIGN KEY(order_id) REFERENCES customer_order(id), " +
                "FOREIGN KEY(payment_request_id) REFERENCES payment_request(id), " +
                "UNIQUE(order_id), " +
                "UNIQUE(payment_request_id)" +
                ")";

        String sql6 = "create table if not exists exchange_rate " +
                "(id string PRIMARY KEY, created_at integer, provider_name string, rate_type string, base_currency string, term_currency string, factor string)";

        for (String sql : Lists.newArrayList(sql1, sql2, sql3, sql4, sql5, sql6)) {
            try (PreparedStatement statement = context.getConnection().prepareStatement(sql)) {
                statement.execute();
            }
        }
    }

}