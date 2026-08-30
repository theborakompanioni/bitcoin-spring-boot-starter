package org.tbk.electrum.example.shell.command;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.CommandGroup;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;
import org.tbk.electrum.ElectrumClient;
import org.tbk.electrum.model.TxHashAndBlockHeight;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

@Slf4j
@Component
@CommandGroup(name = "Commands")
@RequiredArgsConstructor
class AddressHistoryCommand {

    @NonNull
    private final ElectrumClient client;

    @NonNull
    private final JsonMapper jsonMapper;

    @Command(name = "getaddresshistory", description = "execute command 'getaddresshistory'")
    public String run(
            @Option(longName = "address", description = "bitcoin address") String address
    ) throws JacksonException {
        List<TxHashAndBlockHeight> result = client.getAddressHistory(address);
        return jsonMapper.writeValueAsString(result);
    }
}