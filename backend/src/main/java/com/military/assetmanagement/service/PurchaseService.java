package com.military.assetmanagement.service;

import com.military.assetmanagement.dto.request.PurchaseRequest;
import com.military.assetmanagement.entity.*;
import com.military.assetmanagement.exception.AccessDeniedCustomException;
import com.military.assetmanagement.exception.ResourceNotFoundException;
import com.military.assetmanagement.repository.PurchaseRepository;
import com.military.assetmanagement.repository.UserRepository;
import com.military.assetmanagement.util.SecurityUtils;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final BaseService baseService;
    private final EquipmentTypeService equipmentTypeService;
    private final UserRepository userRepository;

    public List<Purchase> search(Long baseId, Long equipmentTypeId, LocalDate startDate, LocalDate endDate) {
        Long effectiveBaseId = enforceBaseScope(baseId);
        Specification<Purchase> spec = buildSpec(effectiveBaseId, equipmentTypeId, startDate, endDate);
        return purchaseRepository.findAll(spec);
    }

    public Purchase create(PurchaseRequest request) {
        Long restricted = SecurityUtils.restrictedBaseId();
        if (SecurityUtils.isBaseCommander() && restricted != null && !restricted.equals(request.getBaseId())) {
            throw new AccessDeniedCustomException("Base commanders may only record purchases for their own base");
        }
        // Logistics officers are restricted to their own base too, if assigned one
        if (SecurityUtils.isLogisticsOfficer() && restricted != null && !restricted.equals(request.getBaseId())) {
            throw new AccessDeniedCustomException("Logistics officers may only record purchases for their own base");
        }

        Base base = baseService.findById(request.getBaseId());
        EquipmentType type = equipmentTypeService.findById(request.getEquipmentTypeId());

        Purchase purchase = new Purchase();
        purchase.setBase(base);
        purchase.setEquipmentType(type);
        purchase.setQuantity(request.getQuantity());
        purchase.setUnitCost(request.getUnitCost());
        if (request.getUnitCost() != null) {
            purchase.setTotalCost(request.getUnitCost().multiply(BigDecimal.valueOf(request.getQuantity())));
        }
        purchase.setPurchaseDate(request.getPurchaseDate());
        purchase.setRemarks(request.getRemarks());

        User current = userRepository.findByUsername(SecurityUtils.currentUser().getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
        purchase.setCreatedBy(current);

        return purchaseRepository.save(purchase);
    }

    /**
     * Non-admin users are always scoped to their own base, regardless of what
     * base filter they pass in.
     */
    private Long enforceBaseScope(Long requestedBaseId) {
        Long restricted = SecurityUtils.restrictedBaseId();
        if (restricted == null) {
            return requestedBaseId; // admin - no restriction
        }
        return restricted;
    }

    static Specification<Purchase> buildSpec(Long baseId, Long equipmentTypeId, LocalDate startDate, LocalDate endDate) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (baseId != null) {
                predicates.add(cb.equal(root.get("base").get("id"), baseId));
            }
            if (equipmentTypeId != null) {
                predicates.add(cb.equal(root.get("equipmentType").get("id"), equipmentTypeId));
            }
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("purchaseDate"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("purchaseDate"), endDate));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
