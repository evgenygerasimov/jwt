package com.jwt.security;

import com.jwt.entity.Token;
import com.jwt.repository.TokenRepository;
import com.jwt.utils.MyKeyGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtServiceTest {

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private MyKeyGenerator myKeyGenerator;

    @InjectMocks
    private JwtService jwtService;

    private Token token;

    private final String accessToken = "accessToken";
    private final String refreshToken = "refreshToken";
    private final String username = "testuser";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        token = new Token();
        token.setUsername(username);
        token.setAccessToken(accessToken);
        token.setRefreshToken(refreshToken);
        token.setValid(true);
    }

    @Test
    void shouldReturnAccessToken() {
        String result = jwtService.generateAccessToken(username);

        assertNotNull(result);
    }

    @Test
    void shouldReturnRefreshToken() {
        String result = jwtService.generateRefreshToken(username);

        assertNotNull(result);
    }

    @Test
    void shouldReturnUsernameWhenTokenIsValid() {
        String token = jwtService.generateAccessToken(username);
        String result = jwtService.extractUserName(token);

        assertEquals(username, result);
    }

    @Test
    void shouldThrowExceptionForInvalidToken() {
        assertThrows(RuntimeException.class, () -> jwtService.extractUserName("invalidToken"));
    }

    @Test
    void shouldSaveTokenSuccessfully() {
        jwtService.saveToken(username, accessToken, refreshToken);

        verify(tokenRepository, times(1)).save(any(Token.class));
    }

    @Test
    void shouldInvalidateAccessToken() {
        when(tokenRepository.findByAccessToken(accessToken)).thenReturn(Optional.of(token));

        jwtService.invalidateToken(accessToken);

        verify(tokenRepository, times(1)).save(any(Token.class));
        assertFalse(token.isValid());
    }

    @Test
    void shouldInvalidateRefreshTokenIfAccessTokenNotFound() {
        when(tokenRepository.findByAccessToken(accessToken)).thenReturn(Optional.empty());
        when(tokenRepository.findByRefreshToken(refreshToken)).thenReturn(Optional.of(token));

        jwtService.invalidateToken(refreshToken);

        verify(tokenRepository, times(1)).save(any(Token.class));
        assertFalse(token.isValid());
    }

    @Test
    void shouldDoNothingIfTokenNotFound() {
        when(tokenRepository.findByAccessToken(accessToken)).thenReturn(Optional.empty());
        when(tokenRepository.findByRefreshToken(refreshToken)).thenReturn(Optional.empty());

        jwtService.invalidateToken(accessToken);

        verify(tokenRepository, times(0)).save(any(Token.class));
    }

    @Test
    void shouldReturnTokenForValidAccessToken() {
        when(tokenRepository.findByAccessToken(accessToken)).thenReturn(Optional.of(token));

        Token result = jwtService.getAccessToken(accessToken);

        assertNotNull(result);
        assertEquals(username, result.getUsername());
    }

    @Test
    void shouldReturnTokenForBearerPrefix() {
        when(tokenRepository.findByAccessToken(accessToken)).thenReturn(Optional.of(token));

        Token result = jwtService.getAccessToken("Bearer " + accessToken);

        assertNotNull(result);
        assertEquals(username, result.getUsername());
    }

    @Test
    void shouldReturnAllTokens() {

        when(tokenRepository.findAll()).thenReturn(List.of(token));

        assertNotNull(jwtService.getTokens());
        assertEquals(1, jwtService.getTokens().size());
    }
}
