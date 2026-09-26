package com.military.assetmanagement.service;

import com.military.assetmanagement.dto.request.EquipmentTypeRequest;
import com.military.assetmanagement.entity.EquipmentCategory;
import com.military.assetmanagement.entity.EquipmentType;
import com.military.assetmanagement.exception.BadRequestException;
import com.military.assetmanagement.exception.ResourceNotFoundException;
import com.military.assetmanagement.repository.EquipmentTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipmentTypeService {

    private final EquipmentTypeRepository equipmentTypeRepository;

    public List<EquipmentType> findAll() {
        return equipmentTypeRepository.findAll();
    }

    public EquipmentType findById(Long id) {
        return equipmentTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment type not found with id: " + id));
    }

    public EquipmentType create(EquipmentTypeRequest request) {
        if (equipmentTypeRepository.existsByNameIgnoreCase(request.getName())) {
            throw new BadRequestException("An equipment type with this name already exists");
        }
        EquipmentType type = new EquipmentType();
        type.setName(request.getName());
        type.setCategory(parseCategory(request.getCategory()));
        type.setUnit(request.getUnit() != null ? request.getUnit() : "units");
        return equipmentTypeRepository.save(type);
    }

    public EquipmentType update(Long id, EquipmentTypeRequest request) {
        EquipmentType type = findById(id);
        type.setName(request.getName());
        type.setCategory(parseCategory(request.getCategory()));
        type.setUnit(request.getUnit());
        return equipmentTypeRepository.save(type);
    }

    public void delete(Long id) {
        equipmentTypeRepository.delete(findById(id));
    }

    private EquipmentCategory parseCategory(String value) {
        try {
            return EquipmentCategory.valueOf(value.toUpperCase());
        } catch (Exception e) {
            throw new BadRequestException("Invalid equipment category: " + value);
        }
    }
}
