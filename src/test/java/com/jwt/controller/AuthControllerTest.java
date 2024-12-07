package com.jwt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jwt.dto.TokenDTO;
import com.jwt.security.JwtService;
import com.jwt.service.AuthService;
import com.jwt.utils.AuthRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    private AuthRequest authRequest;
    private TokenDTO loginTokenDTO;
    private TokenDTO refreshTokenDTORequest;
    private TokenDTO refreshTokenDTOResponse;
    private TokenDTO tokenDTO;
    private String logoutResponseSuccess;
    private String logoutResponseAlreadyLoggedOut;

    @BeforeEach
    void setUp() {
        // Setting up common test data for each test

        authRequest = new AuthRequest();
        authRequest.setUsername("username");
        authRequest.setPassword("password");

        loginTokenDTO = new TokenDTO();
        loginTokenDTO.setAccessToken("access-token");
        loginTokenDTO.setRefreshToken("refresh-token");

        refreshTokenDTORequest = new TokenDTO();
        refreshTokenDTORequest.setAccessToken("old-access-token");
        refreshTokenDTORequest.setRefreshToken("refresh-token");

        refreshTokenDTOResponse = new TokenDTO();
        refreshTokenDTOResponse.setAccessToken("new-access-token");
        refreshTokenDTOResponse.setRefreshToken("refresh-token");

        tokenDTO = new TokenDTO();
        tokenDTO.setAccessToken("access-token");
        tokenDTO.setRefreshToken("refresh-token");

        logoutResponseSuccess = "Logged out successfully";
        logoutResponseAlreadyLoggedOut = "You have already logged out";
    }

    @Test
    void shouldLoginSuccessfullyTest() throws Exception {
        Mockito.when(authService.login(Mockito.any(AuthRequest.class))).thenReturn(loginTokenDTO);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    void shouldRefreshTokenSuccessfullyTest() throws Exception {
        Mockito.when(authService.refreshToken(Mockito.any(TokenDTO.class))).thenReturn(refreshTokenDTOResponse);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(refreshTokenDTORequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    void shouldLogoutSuccessfullyTest() throws Exception {
        Mockito.when(authService.logout(Mockito.any(TokenDTO.class))).thenReturn(logoutResponseSuccess);

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(tokenDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string(logoutResponseSuccess));
    }

    @Test
    void shouldReturnAlreadyLoggedOutErrorTest() throws Exception {
        Mockito.when(authService.logout(Mockito.any(TokenDTO.class))).thenReturn(logoutResponseAlreadyLoggedOut);

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(tokenDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(logoutResponseAlreadyLoggedOut));
    }
}
