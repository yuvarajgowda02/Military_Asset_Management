package com.military.assetmanagement.controller;

import com.military.assetmanagement.dto.request.BaseRequest;
import com.military.assetmanagement.entity.Base;
import com.military.assetmanagement.service.BaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bases")
@RequiredArgsConstructor
public class BaseController {

    private final BaseService baseService;

    @GetMapping
    public List<Base> getAll() {
        return baseService.findAll();
    }

    @GetMapping("/{id}")
    public Base getOne(@PathVariable Long id) {
        return baseService.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Base create(@Valid @RequestBody BaseRequest request) {
        return baseService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Base update(@PathVariable Long id, @Valid @RequestBody BaseRequest request) {
        return baseService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        baseService.delete(id);
    }
}
