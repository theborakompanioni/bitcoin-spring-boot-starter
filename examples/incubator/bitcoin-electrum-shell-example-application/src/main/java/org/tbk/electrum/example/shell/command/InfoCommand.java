package org.tbk.electrum.example.shell.command;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.CommandGroup;
import org.springframework.stereotype.Component;
import org.tbk.electrum.ElectrumClient;
import org.tbk.electrum.rpc.command.GetInfoResponse;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Component
@CommandGroup(name = "Commands")
@RequiredArgsConstructor
class InfoCommand {

    @NonNull
    private final ElectrumClient client;

    @NonNull
    private final JsonMapper jsonMapper;

    @Command(name = "getinfo", description = "execute command 'getinfo'")
    public String run() throws JacksonException {
        GetInfoResponse result = client.getInfo();
        return jsonMapper.writeValueAsString(result);
    }
}