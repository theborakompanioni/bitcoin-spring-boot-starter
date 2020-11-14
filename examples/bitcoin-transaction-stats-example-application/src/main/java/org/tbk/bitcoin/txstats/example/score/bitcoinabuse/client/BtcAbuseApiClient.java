package org.tbk.bitcoin.txstats.example.score.bitcoinabuse.client;

import java.io.FileOutputStream;
import java.util.List;

import static java.util.Objects.requireNonNull;

public interface BtcAbuseApiClient {

    /**
     * Check Address
     * <p>
     * GET https://www.bitcoinabuse.com/api/reports/check
     * <p>
     * This report is cached and only updates once per hour.
     * <p>
     * Parameters
     * api_token - Required. Get your API key.
     * address - Required
     * <p>
     * Example
     * https://www.bitcoinabuse.com/api/reports/check?address={ADDRESS}&api_token={API_TOKEN}
     * {
     * "address": "<input>",
     * count: 4
     * }
     */
    CheckResponseDto check(String address);

    /**
     * Lookup Abuse Type
     * <p>
     * This API allows you to look up the abuse_type_id for use with the report address API.
     * <p>
     * Parameters
     * api_token - Required. Get your API key.
     * <p>
     * GET https://www.bitcoinabuse.com/api/abuse-types
     * <p>
     * [
     * {"id":1,"label":"ransomware"},
     * {"id":2,"label":"darknet market"},
     * {"id":3,"label":"bitcoin tumbler"},
     * {"id":4,"label":"blackmail scam"},
     * {"id":5,"label":"sextortion"},
     * {"id":99,"label":"other"}]
     */
    List<AbuseType> abuseTypes();

    /**
     * Complete Download
     * <p>
     * GET https://www.bitcoinabuse.com/api/download/{time_period}
     * <p>
     * Returns a CSV file containing all reports within the given time period. The "1d" file is
     * updated daily in the early morning. The "30d" file is updated weekly on Sunday morning.
     * The "forever" file is updated monthly on the 15th of the month.
     * All updates occur between 2am-3am UTC.
     * <p>
     * Parameters
     * api_token - Required. Get your API key.
     * time_period - Required. Allowed options are 1d, 30d, or forever
     * <p>
     * Example
     * https://www.bitcoinabuse.com/api/download/30d?api_token={API_TOKEN}
     */
    void downloadCsv(DownloadDuration duration, FileOutputStream outputStream);

    enum DownloadDuration {
        ONE_DAY("1d"),
        THIRTY_DAYS("30d"),
        FOREVER("forever");

        private final String duration;

        DownloadDuration(String duration) {
            this.duration = requireNonNull(duration);
        }

        public String getDuration() {
            return duration;
        }

    }
}
