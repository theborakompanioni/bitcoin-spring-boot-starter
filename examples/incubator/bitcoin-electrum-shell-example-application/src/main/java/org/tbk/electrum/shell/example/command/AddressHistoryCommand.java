package org.tbk.electrum.shell.example.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.tbk.electrum.ElectrumClient;
import org.tbk.electrum.model.TxHashAndBlockHeight;

import java.util.List;

@Slf4j
@ShellComponent
@ShellCommandGroup("Commands")
@RequiredArgsConstructor
class AddressHistoryCommand {

    @NonNull
    private final ElectrumClient client;

    @NonNull
    private final JsonMapper jsonMapper;

    @ShellMethod(key = "getaddresshistory", value = "execute command 'getaddresshistory'")
    public String run(
            @ShellOption(value = "address", help = "bitcoin address") String address
    ) throws JsonProcessingException {
        List<TxHashAndBlockHeight> result = client.getAddressHistory(address);
        return jsonMapper.writeValueAsString(result);
    }
}