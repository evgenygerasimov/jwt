package com.jwt.service;

import com.jwt.dto.TokenDTO;
import com.jwt.entity.Token;
import com.jwt.exception.AuthenticationException;
import com.jwt.exception.InvalidTokenExceptionHandler;
import com.jwt.security.JwtService;
import com.jwt.utils.AuthRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthService authService;

    private AuthRequest authRequest;
    private TokenDTO tokenDTO;
    private Authentication authentication;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        authRequest = new AuthRequest();
        authRequest.setUsername("testuser");
        authRequest.setPassword("password");

        tokenDTO = new TokenDTO();
        tokenDTO.setAccessToken("accessToken");
        tokenDTO.setRefreshToken("refreshToken");

        authentication = mock(Authentication.class);
        userDetails = mock(UserDetails.class);
    }

    @Test
    void shouldAuthenticateAndReturnToken() {
        when(userService.isUserBlocked(authRequest.getUsername())).thenReturn(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn(authRequest.getUsername());
        when(jwtService.generateAccessToken(authRequest.getUsername())).thenReturn("newAccessToken");
        when(jwtService.generateRefreshToken(authRequest.getUsername())).thenReturn("newRefreshToken");

        TokenDTO result = authService.login(authRequest);

        assertNotNull(result);
        assertEquals("newAccessToken", result.getAccessToken());
        assertEquals("newRefreshToken", result.getRefreshToken());
        verify(jwtService, times(1)).saveToken(authRequest.getUsername(), "newAccessToken", "newRefreshToken");
    }

    @Test
    void shouldThrowExceptionWhenUserIsBlocked() {
        when(userService.isUserBlocked(authRequest.getUsername())).thenReturn(true);

        assertThrows(AuthenticationException.class, () -> authService.login(authRequest));
        verifyNoInteractions(authenticationManager);
    }

    @Test
    void shouldThrowExceptionWhenAuthenticationFails() {
        when(userService.isUserBlocked(authRequest.getUsername())).thenReturn(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenThrow(new RuntimeException());

        assertThrows(AuthenticationException.class, () -> authService.login(authRequest));
        verify(userService, times(1)).processFailedLogin(authRequest.getUsername());
    }

    @Test
    void shouldGenerateAndSaveNewTokens() {
        when(jwtService.extractUserName(tokenDTO.getRefreshToken())).thenReturn("testuser");
        when(jwtService.generateAccessToken("testuser")).thenReturn("newAccessToken");
        when(jwtService.generateRefreshToken("testuser")).thenReturn("newRefreshToken");

        TokenDTO result = authService.refreshToken(tokenDTO);

        assertNotNull(result);
        assertEquals("newAccessToken", result.getAccessToken());
        assertEquals("newRefreshToken", result.getRefreshToken());
        verify(jwtService, times(1)).invalidateToken(tokenDTO.getRefreshToken());
        verify(jwtService, times(1)).saveToken("testuser", "newAccessToken", "newRefreshToken");
    }

    @Test
    void shouldThrowExceptionForInvalidToken() {

        when(jwtService.extractUserName(tokenDTO.getRefreshToken())).thenThrow(new RuntimeException());

        assertThrows(InvalidTokenExceptionHandler.class, () -> authService.refreshToken(tokenDTO));
    }

    @Test
    void shouldInvalidateAccessToken() {
        Token token = new Token();
        token.setValid(true);

        when(jwtService.getAccessToken(tokenDTO.getAccessToken())).thenReturn(token);

        String result = authService.logout(tokenDTO);

        assertEquals("Successfully logged out", result);
        verify(jwtService, times(1)).invalidateToken(tokenDTO.getAccessToken());
    }

    @Test
    void shouldReturnAlreadyLoggedOutMessageForInvalidToken() {
        Token token = new Token();
        token.setValid(false);

        when(jwtService.getAccessToken(tokenDTO.getAccessToken())).thenReturn(token);

        String result = authService.logout(tokenDTO);

        assertEquals("You have already logged out", result);
        verify(jwtService, times(0)).invalidateToken(tokenDTO.getAccessToken());
    }
}
