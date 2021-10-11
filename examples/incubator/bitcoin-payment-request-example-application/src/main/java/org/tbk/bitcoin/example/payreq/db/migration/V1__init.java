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

        String sql1 = "create table if not exists customer_order "
                + "(id string PRIMARY KEY, version integer, created_at integer, updated_at integer, status string)";

        String sql2 = "create table if not exists customer_order_line_item "
                + "(id string PRIMARY KEY, order_id string, position integer, name string, price string, currency_unit string, display_price string, quantity integer, "
                + "FOREIGN KEY(order_id) REFERENCES customer_order(id)"
                + ")";

        String sql3 = "create table if not exists payment_request "
                + "(id string PRIMARY KEY, version integer, created_at integer, updated_at integer, order_id string, dtype string, status string, valid_until integer, network string, address string, amount string, currency_unit string, display_price string, "
                + "FOREIGN KEY(order_id) REFERENCES customer_order(id)"
                + ")";

        String sql4 = "create table if not exists invoice "
                + "(id string PRIMARY KEY, version integer, created_at integer, order_id string, comment string,"
                + "FOREIGN KEY(order_id) REFERENCES customer_order(id)"
                + ")";

        String sql5 = "create table if not exists donation "
                + "(id string PRIMARY KEY, version integer, created_at integer, order_id string, payment_request_id string, display_price string, description string, payment_url string, comment string, "
                + "FOREIGN KEY(order_id) REFERENCES customer_order(id), "
                + "FOREIGN KEY(payment_request_id) REFERENCES payment_request(id), "
                + "UNIQUE(order_id), "
                + "UNIQUE(payment_request_id)"
                + ")";

        String sql6 = "create table if not exists exchange_rate "
                + "(id string PRIMARY KEY, created_at integer, provider_name string, rate_type string, base_currency string, term_currency string, factor string)";

        String sql7 = "create table if not exists bitcoin_block "
                + "(id string PRIMARY KEY, "
                + "created_at integer, "
                + "updated_at integer, "
                + "hash string NOT NULL, "
                + "time integer NOT NULL,"
                + "nonce integer NOT NULL, "
                + "confirmations integer NOT NULL, "
                + "size integer NOT NULL, "
                + "height integer NOT NULL, "
                + "version integer NOT NULL, "
                + "previousblockhash string NOT NULL, "
                + "nextblockhash string"
                + ")";

        String sql8 = "create table if not exists bitcoin_chain_info "
                + "(id string PRIMARY KEY, created_at integer, chain string, blocks integer, headers integer, "
                + "best_block_hash string, difficulty string, verification_progress string, chain_work string, "
                + "UNIQUE(chain, best_block_hash) "
                + ")";

        String sql9 = "create table if not exists lnd_info "
                + "(id string PRIMARY KEY, created_at integer, block_height integer, block_hash string, best_header_timestamp integer, "
                + "UNIQUE(block_hash) "
                + ")";

        for (String sql : Lists.newArrayList(sql1, sql2, sql3, sql4, sql5, sql6, sql7, sql8, sql9)) {
            try (PreparedStatement statement = context.getConnection().prepareStatement(sql)) {
                statement.execute();
            }
        }
    }

}