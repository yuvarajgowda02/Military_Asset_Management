package com.military.assetmanagement.controller;

import com.military.assetmanagement.dto.request.TransferRequest;
import com.military.assetmanagement.entity.Transfer;
import com.military.assetmanagement.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/transfers")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','BASE_COMMANDER','LOGISTICS_OFFICER')")
public class TransferController {

    private final TransferService transferService;

    @GetMapping
    public List<Transfer> search(
            @RequestParam(required = false) Long baseId,
            @RequestParam(required = false) Long equipmentTypeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return transferService.search(baseId, equipmentTypeId, startDate, endDate);
    }

    @PostMapping
    public Transfer create(@Valid @RequestBody TransferRequest request) {
        return transferService.create(request);
    }
}
