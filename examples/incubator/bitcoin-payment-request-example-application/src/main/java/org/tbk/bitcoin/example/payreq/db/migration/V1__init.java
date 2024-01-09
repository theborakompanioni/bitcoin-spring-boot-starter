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
        String sql1 = """
                create table if not exists customer_order (
                    id text PRIMARY KEY,
                    version integer,
                    created_at integer,
                    updated_at integer,
                    status text
                ) STRICT;
                """;
        String sql2 = """
                create table if not exists customer_order_line_item (
                    id text PRIMARY KEY,
                    order_id text,
                    position integer,
                    name text,
                    price text,
                    currency_unit text,
                    display_price text,
                    quantity integer,
                    FOREIGN KEY(order_id) REFERENCES customer_order(id)
                ) STRICT;
                """;
        String sql3 = "create table if not exists payment_request "
                      + "(id text PRIMARY KEY, version integer, created_at integer, updated_at integer, order_id text, dtype text, status text, valid_until integer, amount text, currency_unit text, display_price text, "
                      + "network text, " // for all btc payments (onchain, lightning, etc.)
                      + "address text, min_confirmations integer, " // for onchain payments
                      + "payment_hash text, r_hash text, " // for lightning payments
                      + "FOREIGN KEY(order_id) REFERENCES customer_order(id), "
                      + "UNIQUE(address), "
                      + "UNIQUE(r_hash) "
                      + ") "
                      + "STRICT";
        String sql4 = """
                create table if not exists invoice (
                    id text PRIMARY KEY,
                    version integer,
                    created_at integer,
                    order_id text,
                    comment text,
                    FOREIGN KEY(order_id) REFERENCES customer_order(id)
                ) STRICT;
                """;
        String sql5 = """
                create table if not exists donation (
                    id text PRIMARY KEY,
                    version integer,
                    created_at integer,
                    order_id text,
                    payment_request_id text,
                    display_price text,
                    description text,
                    payment_url text,
                    comment text,
                    FOREIGN KEY(order_id) REFERENCES customer_order(id),
                    FOREIGN KEY(payment_request_id) REFERENCES payment_request(id),
                    UNIQUE(order_id),
                    UNIQUE(payment_request_id)
                ) STRICT;
                """;
        String sql6 = """
                create table if not exists exchange_rate (
                    id text PRIMARY KEY,
                    created_at integer,
                    provider_name text,
                    rate_type text,
                    base_currency text,
                    term_currency text,
                    factor text
                ) STRICT;
                """;
        String sql7 = """
                create table if not exists bitcoin_block (
                    id text PRIMARY KEY,
                    created_at integer,
                    updated_at integer,
                    hash text NOT NULL,
                    time integer NOT NULL,
                    nonce integer NOT NULL,
                    confirmations integer NOT NULL,
                    size integer NOT NULL,
                    height integer NOT NULL,
                    version integer NOT NULL,
                    previousblockhash text NOT NULL,
                    nextblockhash text
                ) STRICT;
                """;
        String sql8 = """
                create table if not exists bitcoin_chain_info (
                    id text PRIMARY KEY,
                    created_at integer,
                    chain text,
                    blocks integer,
                    headers integer,
                    best_block_hash text, difficulty text, verification_progress text, chain_work text,
                    UNIQUE(chain, best_block_hash)
                ) STRICT;
                """;
        String sql9 = """
                create table if not exists lnd_info (
                    id text PRIMARY KEY,
                    created_at integer,
                    block_height integer,
                    block_hash text,
                    best_header_timestamp integer,
                    UNIQUE(block_hash)
                ) STRICT;
                """;

        for (String sql : Lists.newArrayList(sql1, sql2, sql3, sql4, sql5, sql6, sql7, sql8, sql9)) {
            try (PreparedStatement statement = context.getConnection().prepareStatement(sql)) {
                statement.execute();
            }
        }
    }
}
