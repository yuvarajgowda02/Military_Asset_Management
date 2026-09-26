package com.military.assetmanagement.service;

import com.military.assetmanagement.dto.response.DashboardResponse;
import com.military.assetmanagement.dto.response.MovementDetailResponse;
import com.military.assetmanagement.entity.*;
import com.military.assetmanagement.repository.*;
import com.military.assetmanagement.util.SecurityUtils;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Aggregates purchase / transfer / assignment / expenditure records into the
 * headline dashboard metrics. Balances are computed on the fly (rather than
 * stored) so that they always stay consistent with the underlying ledger of
 * transactions:
 *
 *   Opening Balance = everything recorded strictly BEFORE the period start
 *   Net Movement     = Purchases + TransferIn - TransferOut (WITHIN the period)
 *   Closing Balance  = Opening Balance + Net Movement - Expended (within period)
 *   Assigned         = quantity currently assigned (not yet returned), within scope
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final PurchaseRepository purchaseRepository;
    private final TransferRepository transferRepository;
    private final AssignmentRepository assignmentRepository;
    private final ExpenditureRepository expenditureRepository;

    public DashboardResponse getMetrics(Long baseId, Long equipmentTypeId, LocalDate startDate, LocalDate endDate) {
        Long restricted = SecurityUtils.restrictedBaseId();
        Long effectiveBaseId = restricted != null ? restricted : baseId;

        LocalDate effectiveStart = startDate != null ? startDate : LocalDate.of(2000, 1, 1);
        LocalDate effectiveEnd = endDate != null ? endDate : LocalDate.now();
        LocalDate dayBeforeStart = effectiveStart.minusDays(1);

        long purchasesBefore = sumPurchases(effectiveBaseId, equipmentTypeId, null, dayBeforeStart);
        long transferInBefore = sumTransfersIn(effectiveBaseId, equipmentTypeId, null, dayBeforeStart);
        long transferOutBefore = sumTransfersOut(effectiveBaseId, equipmentTypeId, null, dayBeforeStart);
        long expendedBefore = sumExpenditure(effectiveBaseId, equipmentTypeId, null, dayBeforeStart);

        long openingBalance = purchasesBefore + transferInBefore - transferOutBefore - expendedBefore;

        long purchases = sumPurchases(effectiveBaseId, equipmentTypeId, effectiveStart, effectiveEnd);
        long transferIn = sumTransfersIn(effectiveBaseId, equipmentTypeId, effectiveStart, effectiveEnd);
        long transferOut = sumTransfersOut(effectiveBaseId, equipmentTypeId, effectiveStart, effectiveEnd);
        long expended = sumExpenditure(effectiveBaseId, equipmentTypeId, effectiveStart, effectiveEnd);
        long assigned = sumCurrentlyAssigned(effectiveBaseId, equipmentTypeId, effectiveEnd);

        long netMovement = purchases + transferIn - transferOut;
        long closingBalance = openingBalance + netMovement - expended;

        return new DashboardResponse(openingBalance, closingBalance, purchases, transferIn, transferOut,
                netMovement, assigned, expended);
    }

    public MovementDetailResponse getMovementDetails(Long baseId, Long equipmentTypeId, LocalDate startDate, LocalDate endDate) {
        Long restricted = SecurityUtils.restrictedBaseId();
        Long effectiveBaseId = restricted != null ? restricted : baseId;

        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;

        List<Purchase> purchases = purchaseRepository.findAll(
                purchaseSpec(effectiveBaseId, equipmentTypeId, startDate, endDate));
        List<MovementDetailResponse.PurchaseView> purchaseViews = purchases.stream()
                .map(p -> new MovementDetailResponse.PurchaseView(
                        p.getId(), p.getBase().getName(), p.getEquipmentType().getName(),
                        p.getQuantity(), p.getPurchaseDate().format(fmt)))
                .toList();

        List<Transfer> transfersIn = transferRepository.findAll(
                transferInSpec(effectiveBaseId, equipmentTypeId, startDate, endDate));
        List<MovementDetailResponse.TransferView> transferInViews = transfersIn.stream()
                .map(t -> new MovementDetailResponse.TransferView(
                        t.getId(), t.getFromBase().getName(), t.getToBase().getName(),
                        t.getEquipmentType().getName(), t.getQuantity(), t.getTransferDate().format(fmt)))
                .toList();

        List<Transfer> transfersOut = transferRepository.findAll(
                transferOutSpec(effectiveBaseId, equipmentTypeId, startDate, endDate));
        List<MovementDetailResponse.TransferView> transferOutViews = transfersOut.stream()
                .map(t -> new MovementDetailResponse.TransferView(
                        t.getId(), t.getFromBase().getName(), t.getToBase().getName(),
                        t.getEquipmentType().getName(), t.getQuantity(), t.getTransferDate().format(fmt)))
                .toList();

        return new MovementDetailResponse(purchaseViews, transferInViews, transferOutViews);
    }

    // ---------------------------------------------------------------
    // Aggregation helpers
    // ---------------------------------------------------------------

    private long sumPurchases(Long baseId, Long equipmentTypeId, LocalDate start, LocalDate end) {
        return purchaseRepository.findAll(purchaseSpec(baseId, equipmentTypeId, start, end))
                .stream().mapToLong(Purchase::getQuantity).sum();
    }

    private long sumTransfersIn(Long baseId, Long equipmentTypeId, LocalDate start, LocalDate end) {
        return transferRepository.findAll(transferInSpec(baseId, equipmentTypeId, start, end))
                .stream().mapToLong(Transfer::getQuantity).sum();
    }

    private long sumTransfersOut(Long baseId, Long equipmentTypeId, LocalDate start, LocalDate end) {
        return transferRepository.findAll(transferOutSpec(baseId, equipmentTypeId, start, end))
                .stream().mapToLong(Transfer::getQuantity).sum();
    }

    private long sumExpenditure(Long baseId, Long equipmentTypeId, LocalDate start, LocalDate end) {
        return expenditureRepository.findAll(expenditureSpec(baseId, equipmentTypeId, start, end))
                .stream().mapToLong(Expenditure::getQuantity).sum();
    }

    private long sumCurrentlyAssigned(Long baseId, Long equipmentTypeId, LocalDate asOf) {
        return assignmentRepository.findAll(assignedAsOfSpec(baseId, equipmentTypeId, asOf))
                .stream().mapToLong(Assignment::getQuantity).sum();
    }

    // ---------------------------------------------------------------
    // Specifications
    // ---------------------------------------------------------------

    private Specification<Purchase> purchaseSpec(Long baseId, Long equipmentTypeId, LocalDate start, LocalDate end) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (baseId != null) predicates.add(cb.equal(root.get("base").get("id"), baseId));
            if (equipmentTypeId != null) predicates.add(cb.equal(root.get("equipmentType").get("id"), equipmentTypeId));
            if (start != null) predicates.add(cb.greaterThanOrEqualTo(root.get("purchaseDate"), start));
            if (end != null) predicates.add(cb.lessThanOrEqualTo(root.get("purchaseDate"), end));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Specification<Transfer> transferInSpec(Long baseId, Long equipmentTypeId, LocalDate start, LocalDate end) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (baseId != null) predicates.add(cb.equal(root.get("toBase").get("id"), baseId));
            if (equipmentTypeId != null) predicates.add(cb.equal(root.get("equipmentType").get("id"), equipmentTypeId));
            if (start != null) predicates.add(cb.greaterThanOrEqualTo(root.get("transferDate"), start));
            if (end != null) predicates.add(cb.lessThanOrEqualTo(root.get("transferDate"), end));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Specification<Transfer> transferOutSpec(Long baseId, Long equipmentTypeId, LocalDate start, LocalDate end) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (baseId != null) predicates.add(cb.equal(root.get("fromBase").get("id"), baseId));
            if (equipmentTypeId != null) predicates.add(cb.equal(root.get("equipmentType").get("id"), equipmentTypeId));
            if (start != null) predicates.add(cb.greaterThanOrEqualTo(root.get("transferDate"), start));
            if (end != null) predicates.add(cb.lessThanOrEqualTo(root.get("transferDate"), end));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Specification<Expenditure> expenditureSpec(Long baseId, Long equipmentTypeId, LocalDate start, LocalDate end) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (baseId != null) predicates.add(cb.equal(root.get("base").get("id"), baseId));
            if (equipmentTypeId != null) predicates.add(cb.equal(root.get("equipmentType").get("id"), equipmentTypeId));
            if (start != null) predicates.add(cb.greaterThanOrEqualTo(root.get("expendedDate"), start));
            if (end != null) predicates.add(cb.lessThanOrEqualTo(root.get("expendedDate"), end));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Specification<Assignment> assignedAsOfSpec(Long baseId, Long equipmentTypeId, LocalDate asOf) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (baseId != null) predicates.add(cb.equal(root.get("base").get("id"), baseId));
            if (equipmentTypeId != null) predicates.add(cb.equal(root.get("equipmentType").get("id"), equipmentTypeId));
            predicates.add(cb.lessThanOrEqualTo(root.get("assignedDate"), asOf));
            // still assigned as of "asOf": either never returned, or returned after asOf
            Predicate notReturned = cb.equal(root.get("status"), AssignmentStatus.ASSIGNED);
            Predicate returnedLater = cb.greaterThan(root.get("returnedDate"), asOf);
            predicates.add(cb.or(notReturned, returnedLater));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
