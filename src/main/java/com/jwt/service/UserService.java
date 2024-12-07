package com.jwt.service;

import com.jwt.dto.UserDTO;
import com.jwt.entity.Role;
import com.jwt.entity.User;
import com.jwt.exception.UserExistException;
import com.jwt.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleService roleService;

    public UserService(UserRepository userRepository, RoleService roleService) {
        this.userRepository = userRepository;
        this.roleService = roleService;
    }

    public void save(User user) {
        if (userRepository.findByUsername(user.getUsername()) != null) {
            throw new UserExistException("User with username " + user.getUsername() + " already exist");
        }
        Role role = new Role();
        role.setUsername(user.getUsername());
        role.setAuthority(user.getRole());
        userRepository.save(user);
        roleService.save(role);
    }

    public UserDTO convertUserToUserDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setEnabled(user.isEnabled());
        userDTO.setRole(user.getRole());
        return userDTO;
    }

    public UserDetailsService userDetailsService() {
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String username) {
                User user = userRepository.findByUsername(username);
                if (user == null) {
                    throw new UsernameNotFoundException("User not found with username: " + username);
                }
                return user;
            }
        };
    }

    public boolean isUserBlocked(String username) {
        User user = userRepository.findByUsername(username);
        return user != null && !user.isAccountNonLocked();
    }

    public void processFailedLogin(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) return;

        int attempts = user.getFailedLoginAttempts();
        if (attempts > 4) {
            user.setAccountNonLocked(false);
        } else {
            user.setFailedLoginAttempts(attempts + 1);
        }
        userRepository.save(user);
    }
}
