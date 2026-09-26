package com.military.assetmanagement.controller;

import com.military.assetmanagement.dto.request.EquipmentTypeRequest;
import com.military.assetmanagement.entity.EquipmentType;
import com.military.assetmanagement.service.EquipmentTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/equipment-types")
@RequiredArgsConstructor
public class EquipmentTypeController {

    private final EquipmentTypeService equipmentTypeService;

    @GetMapping
    public List<EquipmentType> getAll() {
        return equipmentTypeService.findAll();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public EquipmentType create(@Valid @RequestBody EquipmentTypeRequest request) {
        return equipmentTypeService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public EquipmentType update(@PathVariable Long id, @Valid @RequestBody EquipmentTypeRequest request) {
        return equipmentTypeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        equipmentTypeService.delete(id);
    }
}
