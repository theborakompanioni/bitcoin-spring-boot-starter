package org.tbk.electrum.example.shell.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Coin;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.tbk.electrum.ElectrumClient;
import org.tbk.electrum.model.Balance;
import org.tbk.electrum.rpc.command.GetInfoResponse;

@Slf4j
@ShellComponent
@ShellCommandGroup("Commands")
@RequiredArgsConstructor
class InfoCommand {

    @NonNull
    private final ElectrumClient client;

    @NonNull
    private final JsonMapper jsonMapper;

    @ShellMethod(key = "getinfo", value = "execute command 'getinfo'")
    public String run() throws JsonProcessingException {
        GetInfoResponse result = client.getInfo();
        return jsonMapper.writeValueAsString(result);
    }
}