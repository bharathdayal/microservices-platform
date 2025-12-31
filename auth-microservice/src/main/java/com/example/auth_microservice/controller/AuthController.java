package com.example.auth_microservice.controller;

import com.example.auth_microservice.domain.LoginRequest;
import com.example.auth_microservice.domain.UserRole;
import com.example.auth_microservice.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;


    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody LoginRequest request) {
        if ("admin".equals(request.username())
                && "admin123".equals(request.password())) {

            return Map.of(
                    "access_token",
                    authService.generateToken(
                            request.username(),
                            UserRole.ADMIN
                    )
            );
        }

        if ("user".equals(request.username())
                && "user123".equals(request.password())) {

            return Map.of(
                    "access_token",
                    authService.generateToken(
                            request.username(),
                            UserRole.USER
                    )
            );
        }

        throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Invalid credentials"
        );

    }


}
