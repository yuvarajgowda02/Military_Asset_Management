package com.military.assetmanagement.repository;

import com.military.assetmanagement.entity.Base;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BaseRepository extends JpaRepository<Base, Long> {
    boolean existsByNameIgnoreCase(String name);
}
