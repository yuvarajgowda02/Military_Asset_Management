package com.military.assetmanagement.service;

import com.military.assetmanagement.dto.request.AssignmentRequest;
import com.military.assetmanagement.entity.*;
import com.military.assetmanagement.exception.AccessDeniedCustomException;
import com.military.assetmanagement.exception.BadRequestException;
import com.military.assetmanagement.exception.ResourceNotFoundException;
import com.military.assetmanagement.repository.AssignmentRepository;
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
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final BaseService baseService;
    private final EquipmentTypeService equipmentTypeService;
    private final UserRepository userRepository;

    public List<Assignment> search(Long baseId, Long equipmentTypeId, LocalDate startDate, LocalDate endDate) {
        Long restricted = SecurityUtils.restrictedBaseId();
        Long effectiveBaseId = restricted != null ? restricted : baseId;
        Specification<Assignment> spec = buildSpec(effectiveBaseId, equipmentTypeId, startDate, endDate);
        return assignmentRepository.findAll(spec);
    }

    public Assignment create(AssignmentRequest request) {
        enforceBaseAccess(request.getBaseId());

        Base base = baseService.findById(request.getBaseId());
        EquipmentType type = equipmentTypeService.findById(request.getEquipmentTypeId());

        Assignment assignment = new Assignment();
        assignment.setBase(base);
        assignment.setEquipmentType(type);
        assignment.setPersonnelName(request.getPersonnelName());
        assignment.setPersonnelServiceNumber(request.getPersonnelServiceNumber());
        assignment.setQuantity(request.getQuantity());
        assignment.setAssignedDate(request.getAssignedDate());
        assignment.setStatus(AssignmentStatus.ASSIGNED);
        assignment.setRemarks(request.getRemarks());

        User current = userRepository.findByUsername(SecurityUtils.currentUser().getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
        assignment.setCreatedBy(current);

        return assignmentRepository.save(assignment);
    }

    public Assignment markReturned(Long id, LocalDate returnedDate) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + id));
        enforceBaseAccess(assignment.getBase().getId());

        if (assignment.getStatus() == AssignmentStatus.RETURNED) {
            throw new BadRequestException("This asset has already been marked as returned");
        }
        assignment.setStatus(AssignmentStatus.RETURNED);
        assignment.setReturnedDate(returnedDate != null ? returnedDate : LocalDate.now());
        return assignmentRepository.save(assignment);
    }

    private void enforceBaseAccess(Long baseId) {
        Long restricted = SecurityUtils.restrictedBaseId();
        if (restricted != null && !restricted.equals(baseId)) {
            throw new AccessDeniedCustomException("You may only manage assignments for your own base");
        }
    }

    static Specification<Assignment> buildSpec(Long baseId, Long equipmentTypeId, LocalDate startDate, LocalDate endDate) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (baseId != null) {
                predicates.add(cb.equal(root.get("base").get("id"), baseId));
            }
            if (equipmentTypeId != null) {
                predicates.add(cb.equal(root.get("equipmentType").get("id"), equipmentTypeId));
            }
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("assignedDate"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("assignedDate"), endDate));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
