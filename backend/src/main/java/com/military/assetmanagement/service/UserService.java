package com.military.assetmanagement.service;

import com.military.assetmanagement.dto.request.UserCreateRequest;
import com.military.assetmanagement.entity.Base;
import com.military.assetmanagement.entity.RoleName;
import com.military.assetmanagement.entity.User;
import com.military.assetmanagement.exception.BadRequestException;
import com.military.assetmanagement.exception.ResourceNotFoundException;
import com.military.assetmanagement.repository.BaseRepository;
import com.military.assetmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BaseRepository baseRepository;
    private final PasswordEncoder passwordEncoder;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User create(UserCreateRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username already taken");
        }
        RoleName role;
        try {
            role = RoleName.valueOf(request.getRole().toUpperCase());
        } catch (Exception e) {
            throw new BadRequestException("Invalid role: " + request.getRole());
        }

        Base base = null;
        if (role != RoleName.ADMIN) {
            if (request.getBaseId() == null) {
                throw new BadRequestException("baseId is required for role " + role);
            }
            base = baseRepository.findById(request.getBaseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Base not found with id: " + request.getBaseId()));
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setRole(role);
        user.setBase(base);
        user.setEnabled(true);
        return userRepository.save(user);
    }

    public void setEnabled(Long id, boolean enabled) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setEnabled(enabled);
        userRepository.save(user);
    }
}
