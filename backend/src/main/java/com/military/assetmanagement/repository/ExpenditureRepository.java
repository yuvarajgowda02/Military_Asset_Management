package com.military.assetmanagement.repository;

import com.military.assetmanagement.entity.Expenditure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ExpenditureRepository extends JpaRepository<Expenditure, Long>, JpaSpecificationExecutor<Expenditure> {
}
