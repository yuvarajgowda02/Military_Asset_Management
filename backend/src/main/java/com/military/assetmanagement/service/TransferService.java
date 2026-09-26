package com.military.assetmanagement.service;

import com.military.assetmanagement.dto.request.TransferRequest;
import com.military.assetmanagement.entity.*;
import com.military.assetmanagement.exception.AccessDeniedCustomException;
import com.military.assetmanagement.exception.BadRequestException;
import com.military.assetmanagement.exception.ResourceNotFoundException;
import com.military.assetmanagement.repository.TransferRepository;
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
public class TransferService {

    private final TransferRepository transferRepository;
    private final BaseService baseService;
    private final EquipmentTypeService equipmentTypeService;
    private final UserRepository userRepository;

    public List<Transfer> search(Long baseId, Long equipmentTypeId, LocalDate startDate, LocalDate endDate) {
        Long restricted = SecurityUtils.restrictedBaseId();
        Long effectiveBaseId = restricted != null ? restricted : baseId;
        Specification<Transfer> spec = buildSpec(effectiveBaseId, equipmentTypeId, startDate, endDate);
        return transferRepository.findAll(spec);
    }

    public Transfer create(TransferRequest request) {
        if (request.getFromBaseId().equals(request.getToBaseId())) {
            throw new BadRequestException("Source and destination base must be different");
        }

        Long restricted = SecurityUtils.restrictedBaseId();
        if (restricted != null
                && !restricted.equals(request.getFromBaseId())
                && !restricted.equals(request.getToBaseId())) {
            throw new AccessDeniedCustomException("You may only transfer assets involving your own base");
        }

        Base fromBase = baseService.findById(request.getFromBaseId());
        Base toBase = baseService.findById(request.getToBaseId());
        EquipmentType type = equipmentTypeService.findById(request.getEquipmentTypeId());

        Transfer transfer = new Transfer();
        transfer.setFromBase(fromBase);
        transfer.setToBase(toBase);
        transfer.setEquipmentType(type);
        transfer.setQuantity(request.getQuantity());
        transfer.setTransferDate(request.getTransferDate());
        transfer.setStatus(TransferStatus.COMPLETED);
        transfer.setRemarks(request.getRemarks());

        User current = userRepository.findByUsername(SecurityUtils.currentUser().getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
        transfer.setCreatedBy(current);

        return transferRepository.save(transfer);
    }

    static Specification<Transfer> buildSpec(Long baseId, Long equipmentTypeId, LocalDate startDate, LocalDate endDate) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (baseId != null) {
                Predicate fromMatch = cb.equal(root.get("fromBase").get("id"), baseId);
                Predicate toMatch = cb.equal(root.get("toBase").get("id"), baseId);
                predicates.add(cb.or(fromMatch, toMatch));
            }
            if (equipmentTypeId != null) {
                predicates.add(cb.equal(root.get("equipmentType").get("id"), equipmentTypeId));
            }
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("transferDate"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("transferDate"), endDate));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
