package org.tbk.bitcoin.regtest.example.api;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbk.electrum.ElectrumClient;
import org.tbk.electrum.command.GetInfoResponse;

@RestController
@RequestMapping("/api/v1/electrum")
@RequiredArgsConstructor
public class ElectrumDaemonApi {

    @NonNull
    private final ElectrumClient electrumClient;

    @GetMapping(path = "/status")
    public ResponseEntity<GetInfoResponse> status() {
        return ResponseEntity.ok(electrumClient.getInfo());
    }
}
