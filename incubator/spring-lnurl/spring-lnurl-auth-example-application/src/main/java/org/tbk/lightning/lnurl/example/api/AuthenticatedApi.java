package org.tbk.lightning.lnurl.example.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/authenticated")
@RequiredArgsConstructor
public class AuthenticatedApi {

    @GetMapping(path = "/self", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDetails> authenticationPrincipalUserDetails(@AuthenticationPrincipal(errorOnInvalidType = true) UserDetails currentUser) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(currentUser);
    }
}
