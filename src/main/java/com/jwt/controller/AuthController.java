package com.jwt.controller;

import com.jwt.dto.TokenDTO;
import com.jwt.utils.AuthRequest;
import com.jwt.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        TokenDTO tokenDTO = authService.login(authRequest);
        return ResponseEntity.ok(tokenDTO);
    }

    @PostMapping("/refresh")
    public TokenDTO refresh(@RequestBody TokenDTO tokenDTO) {
        return authService.refreshToken(tokenDTO);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody TokenDTO tokenDTO) {
        String response = authService.logout(tokenDTO);
        if (response.equals("You have already logged out")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        return ResponseEntity.ok(response);
    }
}