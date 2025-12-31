package com.example.auth_microservice.controller;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth/.well-known")
public class JwksController {

    private final JWKSet jwkSet;

    public JwksController(JWKSet jwkSet) {
        this.jwkSet=jwkSet;
    }

    @GetMapping("/jwks.json")
    public Map<String,Object> keys() {
        return new JWKSet(
                jwkSet.getKeys()
                        .stream()
                        .map(JWK::toPublicJWK)
                        .toList()
        ).toJSONObject();
    }
}
