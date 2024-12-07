package com.jwt.service;

import com.jwt.entity.Role;
import com.jwt.repository.RoleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    public void save(Role role) {
        roleRepository.save(role);
    }
}

