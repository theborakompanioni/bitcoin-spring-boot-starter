package org.tbk.tor.spring.example.api;

import com.google.common.collect.ImmutableMap;
import io.micrometer.core.annotation.Timed;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.berndpruenster.netlayer.tor.Tor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping(value = "/api/v1/tor")
@RequiredArgsConstructor
public class TorCtrl {

    @NonNull
    private final Tor tor;

    @GetMapping
    @Timed
    public ResponseEntity<? extends Map<String, Object>> info() {
        Map<String, Object> result = ImmutableMap.<String, Object>builder()
                .put("class", tor.getClass().getName())
                .build();

        return ResponseEntity.ok(result);
    }
}
