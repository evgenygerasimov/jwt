package com.jwt.controller;

import com.jwt.entity.Token;
import com.jwt.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc
class EndpointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private EndpointController endpointController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(endpointController).build();
    }

    @Test
    void shouldReturnHelloWorldTest() throws Exception {
        mockMvc.perform(get("/api/endpoint/for-everyone"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello World!"));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void shouldReturnHelloAdminTest() throws Exception {
        Token validToken = new Token();
        validToken.setValid(true);

        when(jwtService.getAccessToken(anyString())).thenReturn(validToken);

        mockMvc.perform(get("/api/endpoint/for-admin")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer validToken"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello Admin!"));

        verify(jwtService, times(1)).getAccessToken("Bearer validToken");
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void shouldReturnUnauthorizedForInvalidAdminTokenTest() throws Exception {
        Token invalidToken = new Token();
        invalidToken.setValid(false);

        when(jwtService.getAccessToken(anyString())).thenReturn(invalidToken);

        mockMvc.perform(get("/api/endpoint/for-admin")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalidToken"))
                .andExpect(status().isUnauthorized());

        verify(jwtService, times(1)).getAccessToken("Bearer invalidToken");
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void shouldReturnSuccessForValidUserTokenTest() throws Exception {
        Token validToken = new Token();
        validToken.setValid(true);

        when(jwtService.getAccessToken(anyString())).thenReturn(validToken);

        mockMvc.perform(get("/api/endpoint/for-user")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer validToken"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello User!"));
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void shouldReturnUnauthorizedForInvalidUserTokenTest() throws Exception {
        Token invalidToken = new Token();
        invalidToken.setValid(false);

        when(jwtService.getAccessToken(anyString())).thenReturn(invalidToken);

        mockMvc.perform(get("/api/endpoint/for-user")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalidToken"))
                .andExpect(status().isUnauthorized());

        verify(jwtService, times(1)).getAccessToken("Bearer invalidToken");
    }

    @Test
    @WithMockUser(authorities = "ROLE_SUPER_ADMIN")
    void shouldReturnSuccessForValidSuperAdminTokenTest() throws Exception {
        Token validToken = new Token();
        validToken.setValid(true);

        when(jwtService.getAccessToken(anyString())).thenReturn(validToken);

        mockMvc.perform(get("/api/endpoint/for-super-admin")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer validToken"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello Super Admin!"));
    }


    @Test
    @WithMockUser(authorities = "ROLE_SUPER_ADMIN")
    void shouldReturnUnauthorizedForInvalidSuperAdminTokenTest() throws Exception {
        Token invalidToken = new Token();
        invalidToken.setValid(false);

        when(jwtService.getAccessToken(anyString())).thenReturn(invalidToken);

        mockMvc.perform(get("/api/endpoint/for-super-admin")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalidToken"))
                .andExpect(status().isUnauthorized());

        verify(jwtService, times(1)).getAccessToken("Bearer invalidToken");
    }
}

