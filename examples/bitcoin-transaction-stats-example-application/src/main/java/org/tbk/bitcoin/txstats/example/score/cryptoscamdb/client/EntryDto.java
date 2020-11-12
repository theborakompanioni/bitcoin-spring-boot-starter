package org.tbk.bitcoin.txstats.example.score.cryptoscamdb.client;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import javax.annotation.Nullable;

@Value
@Builder
@Jacksonized
public class EntryDto {
    String id; // e.g. "a73833"
    String name; // 	"eth.ug"
    String type; // 	"scam"
    String url; // 	"http://eth.ug"
    String hostname; // 	"eth.ug"
    int featured; // 	0
    String path; // 	"/*"
    String category; // 	"Scamming"
    String subcategory; // 	"Trust-Trading"
    String description; // 	"Trust trading scam site"
    String reporter; // 	"CryptoScamDB"

    @Nullable
    String ip; // 	null
    int severity; // 	1

    @Nullable
    Object statusCode; // 	null

    @Nullable
    Object status; // 	null
    long updated; // 	1605101148555
    String address; // "16wd9B1LiXmTNf9hxQyb3Q9fbVHzP3NvSV"
}
