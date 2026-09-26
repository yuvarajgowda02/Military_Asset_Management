package com.military.assetmanagement.controller;

import com.military.assetmanagement.dto.request.UserCreateRequest;
import com.military.assetmanagement.entity.User;
import com.military.assetmanagement.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    @GetMapping
    public List<User> getAll() {
        return userService.findAll();
    }

    @PostMapping
    public User create(@Valid @RequestBody UserCreateRequest request) {
        return userService.create(request);
    }

    @PatchMapping("/{id}/enabled")
    public void setEnabled(@PathVariable Long id, @RequestParam boolean enabled) {
        userService.setEnabled(id, enabled);
    }
}
