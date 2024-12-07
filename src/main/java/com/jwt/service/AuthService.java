package com.jwt.service;

import com.jwt.dto.TokenDTO;
import com.jwt.entity.Token;
import com.jwt.exception.AuthenticationException;
import com.jwt.exception.InvalidTokenExceptionHandler;
import com.jwt.utils.AuthRequest;
import com.jwt.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userService = userService;
    }

    public TokenDTO login(AuthRequest authRequest) {
        if (userService.isUserBlocked(authRequest.getUsername())) {
            throw new AuthenticationException("Account is blocked due to multiple failed login attempts");
        }
        TokenDTO tokenDTO = new TokenDTO();
        for (Token token : jwtService.getTokens()) {
            if (token.getUsername().equals(authRequest.getUsername()) && token.isValid()) {
                tokenDTO.setAccessToken(token.getAccessToken());
                tokenDTO.setRefreshToken(token.getRefreshToken());
                return tokenDTO;
            }
        }
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword());
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(authToken);
        } catch (Exception e) {
            userService.processFailedLogin(authRequest.getUsername());
            throw new AuthenticationException("Invalid username or password");
        }
        if (authentication.isAuthenticated()) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String accessToken = jwtService.generateAccessToken(userDetails.getUsername());
            String refreshToken = jwtService.generateRefreshToken(userDetails.getUsername());
            tokenDTO.setAccessToken(accessToken);
            tokenDTO.setRefreshToken(refreshToken);
            jwtService.saveToken(userDetails.getUsername(), accessToken, refreshToken);
        }
        return tokenDTO;
    }

    public TokenDTO refreshToken(TokenDTO tokenDTO) {
        String refreshToken = tokenDTO.getRefreshToken();
        String username;
        try {
            username = jwtService.extractUserName(refreshToken);

        } catch (Exception e) {
            throw new InvalidTokenExceptionHandler("Invalid refresh token");
        }
        jwtService.invalidateToken(refreshToken);
        String newAccessToken = jwtService.generateAccessToken(username);
        String newRefreshToken = jwtService.generateRefreshToken(username);
        jwtService.saveToken(username, newAccessToken, newRefreshToken);
        TokenDTO newTokenDTO = new TokenDTO();
        newTokenDTO.setAccessToken(newAccessToken);
        newTokenDTO.setRefreshToken(newRefreshToken);
        return newTokenDTO;
    }

    public String logout(TokenDTO tokenDTO) {
        Token token = jwtService.getAccessToken(tokenDTO.getAccessToken());
        if (!token.isValid()) {
            return "You have already logged out";
        }
        String accessToken = tokenDTO.getAccessToken();
        jwtService.invalidateToken(accessToken);
        return "Successfully logged out";
    }
}
