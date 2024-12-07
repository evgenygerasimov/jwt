package com.jwt.service;

import com.jwt.dto.UserDTO;
import com.jwt.entity.Role;
import com.jwt.entity.User;
import com.jwt.exception.UserExistException;
import com.jwt.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleService roleService;

    @InjectMocks
    private UserService userService;

    private User user;
    private Role role;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("password");
        user.setRole("ROLE_USER");
        user.isAccountNonLocked();
        user.setAccountNonLocked(true);
        user.setFailedLoginAttempts(0);

        role = new Role();
        role.setUsername("testuser");
        role.setAuthority("ROLE_USER");
    }

    @Test
    void shouldSaveUserSuccessfullyTest() {
        when(userRepository.findByUsername(user.getUsername())).thenReturn(null);
        userService.save(user);
        verify(userRepository, times(1)).save(user);
        verify(roleService, times(1)).save(any(Role.class));
    }

    @Test
    void shouldThrowExceptionWhenUserAlreadyExistsTest() {
        when(userRepository.findByUsername(user.getUsername())).thenReturn(user);
        assertThrows(UserExistException.class, () -> userService.save(user));
    }

    @Test
    void shouldConvertUserToUserDTOTest() {
        UserDTO userDTO = userService.convertUserToUserDTO(user);
        assertNotNull(userDTO);
        assertEquals(user.getId(), userDTO.getId());
        assertEquals(user.getUsername(), userDTO.getUsername());
        assertEquals(user.getRole(), userDTO.getRole());
        assertEquals(user.isEnabled(), userDTO.isEnabled());
    }

    @Test
    void shouldLoadUserByUsernameSuccessfullyTest() {
        when(userRepository.findByUsername(user.getUsername())).thenReturn(user);
        UserDetails userDetails = userService.userDetailsService().loadUserByUsername(user.getUsername());
        assertNotNull(userDetails);
        assertEquals(user.getUsername(), userDetails.getUsername());
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundTest() {
        when(userRepository.findByUsername(user.getUsername())).thenReturn(null);
        UserDetailsService userDetailsService = userService.userDetailsService();
        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername(user.getUsername()));
    }

    @Test
    void shouldReturnUserBlockedStateTest() {
        when(userRepository.findByUsername(user.getUsername())).thenReturn(user);
        user.setAccountNonLocked(false);
        assertTrue(userService.isUserBlocked(user.getUsername()));
    }

    @Test
    void shouldReturnUserNotBlockedStateTest() {
        when(userRepository.findByUsername(user.getUsername())).thenReturn(user);
        user.setAccountNonLocked(true);
        assertFalse(userService.isUserBlocked(user.getUsername()));
    }

    @Test
    void shouldProcessFailedLoginAndLockAccountTest() {
        user.setFailedLoginAttempts(5);
        when(userRepository.findByUsername(user.getUsername())).thenReturn(user);

        userService.processFailedLogin(user.getUsername());

        assertFalse(user.isAccountNonLocked());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void shouldProcessFailedLoginAndIncrementAttemptsTest() {
        user.setFailedLoginAttempts(2);
        when(userRepository.findByUsername(user.getUsername())).thenReturn(user);

        userService.processFailedLogin(user.getUsername());

        assertEquals(3, user.getFailedLoginAttempts());
        assertTrue(user.isAccountNonLocked());
        verify(userRepository, times(1)).save(user);
    }

    @Test

    void shouldDoNothingIfUserNotFoundDuringFailedLoginTest() {
        when(userRepository.findByUsername(user.getUsername())).thenReturn(null);

        userService.processFailedLogin(user.getUsername());

        verify(userRepository, never()).save(any());
    }
}
