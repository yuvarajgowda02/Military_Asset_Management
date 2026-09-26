package com.military.assetmanagement.repository;

import com.military.assetmanagement.entity.EquipmentType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipmentTypeRepository extends JpaRepository<EquipmentType, Long> {
    boolean existsByNameIgnoreCase(String name);
}
