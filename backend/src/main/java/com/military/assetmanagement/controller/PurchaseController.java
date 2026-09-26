package com.military.assetmanagement.controller;

import com.military.assetmanagement.dto.request.PurchaseRequest;
import com.military.assetmanagement.entity.Purchase;
import com.military.assetmanagement.service.PurchaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/purchases")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','BASE_COMMANDER','LOGISTICS_OFFICER')")
public class PurchaseController {

    private final PurchaseService purchaseService;

    @GetMapping
    public List<Purchase> search(
            @RequestParam(required = false) Long baseId,
            @RequestParam(required = false) Long equipmentTypeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return purchaseService.search(baseId, equipmentTypeId, startDate, endDate);
    }

    @PostMapping
    public Purchase create(@Valid @RequestBody PurchaseRequest request) {
        return purchaseService.create(request);
    }
}
