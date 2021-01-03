package org.tbk.spring.testcontainer.tor.example.api;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping(value = "/api/v1/tor")
@RequiredArgsConstructor
public class TorCtrl {
    
    @GetMapping(path = "/info")
    public ResponseEntity<? extends Map<String, Object>> provider() {
        Map<String, Object> result = ImmutableMap.<String, Object>builder()
                .build();

        return ResponseEntity.ok(result);
    }
}
