package com.military.assetmanagement.controller;

import com.military.assetmanagement.dto.request.LoginRequest;
import com.military.assetmanagement.dto.response.LoginResponse;
import com.military.assetmanagement.entity.User;
import com.military.assetmanagement.repository.UserRepository;
import com.military.assetmanagement.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public User me(org.springframework.security.core.Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new com.military.assetmanagement.exception.ResourceNotFoundException("User not found"));
    }
}
