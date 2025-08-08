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
import org.tbk.electrum.model.Utxos;

@Slf4j
@ShellComponent
@ShellCommandGroup("Commands")
@RequiredArgsConstructor
class AddressUnspentCommand {

    @NonNull
    private final ElectrumClient client;

    @NonNull
    private final JsonMapper jsonMapper;

    @ShellMethod(key = "getaddressunspent", value = "execute command 'getaddressunspent'")
    public String run(
            @ShellOption(value = "address", help = "bitcoin address") String address
    ) throws JsonProcessingException {
        Utxos result = client.getUtxosByAddress(address);
        return jsonMapper.writeValueAsString(result);
    }
}