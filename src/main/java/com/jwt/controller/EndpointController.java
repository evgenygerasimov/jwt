package com.jwt.controller;

import com.jwt.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/endpoint")
public class EndpointController {

    private final JwtService jwtService;
    public EndpointController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/for-everyone")
    public String hello() {
        return "Hello World!";
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/for-admin")
    public ResponseEntity<String> helloAdmin(@RequestHeader("Authorization") String token) {
        if (!jwtService.getAccessToken(token).isValid()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok("Hello Admin!");
    }

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @GetMapping("/for-user")
    public ResponseEntity<String> helloUser(@RequestHeader("Authorization") String token) {
        if (!jwtService.getAccessToken(token).isValid()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok("Hello User!");
    }

    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    @GetMapping("/for-super-admin")
    public ResponseEntity<String> helloSuperAdmin(@RequestHeader("Authorization") String token) {
        if (!jwtService.getAccessToken(token).isValid()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok("Hello Super Admin!");
    }
}
