package com.military.assetmanagement.service;

import com.military.assetmanagement.dto.request.LoginRequest;
import com.military.assetmanagement.dto.response.LoginResponse;
import com.military.assetmanagement.entity.User;
import com.military.assetmanagement.repository.UserRepository;
import com.military.assetmanagement.security.JwtUtil;
import com.military.assetmanagement.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found after authentication"));

        UserPrincipal principal = new UserPrincipal(user);
        String token = jwtUtil.generateToken(principal, user.getId(), user.getRole().name(),
                user.getBase() != null ? user.getBase().getId() : null);

        return new LoginResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getRole().name(),
                user.getBase() != null ? user.getBase().getId() : null,
                user.getBase() != null ? user.getBase().getName() : null
        );
    }
}
