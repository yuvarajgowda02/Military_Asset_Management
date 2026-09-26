package com.military.assetmanagement.service;

import com.military.assetmanagement.dto.request.ExpenditureRequest;
import com.military.assetmanagement.entity.*;
import com.military.assetmanagement.exception.AccessDeniedCustomException;
import com.military.assetmanagement.exception.ResourceNotFoundException;
import com.military.assetmanagement.repository.ExpenditureRepository;
import com.military.assetmanagement.repository.UserRepository;
import com.military.assetmanagement.util.SecurityUtils;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenditureService {

    private final ExpenditureRepository expenditureRepository;
    private final BaseService baseService;
    private final EquipmentTypeService equipmentTypeService;
    private final UserRepository userRepository;

    public List<Expenditure> search(Long baseId, Long equipmentTypeId, LocalDate startDate, LocalDate endDate) {
        Long restricted = SecurityUtils.restrictedBaseId();
        Long effectiveBaseId = restricted != null ? restricted : baseId;
        Specification<Expenditure> spec = buildSpec(effectiveBaseId, equipmentTypeId, startDate, endDate);
        return expenditureRepository.findAll(spec);
    }

    public Expenditure create(ExpenditureRequest request) {
        Long restricted = SecurityUtils.restrictedBaseId();
        if (restricted != null && !restricted.equals(request.getBaseId())) {
            throw new AccessDeniedCustomException("You may only record expenditures for your own base");
        }

        Base base = baseService.findById(request.getBaseId());
        EquipmentType type = equipmentTypeService.findById(request.getEquipmentTypeId());

        Expenditure expenditure = new Expenditure();
        expenditure.setBase(base);
        expenditure.setEquipmentType(type);
        expenditure.setQuantity(request.getQuantity());
        expenditure.setExpendedDate(request.getExpendedDate());
        expenditure.setReason(request.getReason());

        User current = userRepository.findByUsername(SecurityUtils.currentUser().getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
        expenditure.setCreatedBy(current);

        return expenditureRepository.save(expenditure);
    }

    static Specification<Expenditure> buildSpec(Long baseId, Long equipmentTypeId, LocalDate startDate, LocalDate endDate) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (baseId != null) {
                predicates.add(cb.equal(root.get("base").get("id"), baseId));
            }
            if (equipmentTypeId != null) {
                predicates.add(cb.equal(root.get("equipmentType").get("id"), equipmentTypeId));
            }
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("expendedDate"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("expendedDate"), endDate));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
