package com.jwt.service;

import com.jwt.entity.Role;
import com.jwt.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleService roleService;

    private Role role;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        role = new Role();
        role.setUsername("testuser");
        role.setAuthority("ROLE_USER");
    }

    @Test
    void shouldSaveRoleSuccessfullyTest() {
        roleService.save(role);
        verify(roleRepository, times(1)).save(role);
    }
}
