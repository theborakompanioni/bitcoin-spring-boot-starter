package org.tbk.bitcoin.fee.example.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbk.bitcoin.tool.fee.FeeRecommendationResponse;

import java.util.Collection;
import java.util.Collections;

@RestController
@RequestMapping(value = "/api/v1/fee")
@RequiredArgsConstructor
public class FeeCtrl {

    @GetMapping
    public ResponseEntity<Collection<FeeRecommendationResponse>> get() {
        return ResponseEntity.ok(Collections.emptyList());
    }
}
