package org.tbk.bitcoin.tool.cryptoscamdb.client;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.Map;

// e.g. see response https://api.cryptoscamdb.org/v1/addresses
@Value
@Builder
@Jacksonized
public class AddressesResponseDto {

    boolean success;

    Map<String, List<EntryDto>> result;


}
