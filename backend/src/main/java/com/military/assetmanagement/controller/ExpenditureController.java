package com.military.assetmanagement.controller;

import com.military.assetmanagement.dto.request.ExpenditureRequest;
import com.military.assetmanagement.entity.Expenditure;
import com.military.assetmanagement.service.ExpenditureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/expenditures")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','BASE_COMMANDER')")
public class ExpenditureController {

    private final ExpenditureService expenditureService;

    @GetMapping
    public List<Expenditure> search(
            @RequestParam(required = false) Long baseId,
            @RequestParam(required = false) Long equipmentTypeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return expenditureService.search(baseId, equipmentTypeId, startDate, endDate);
    }

    @PostMapping
    public Expenditure create(@Valid @RequestBody ExpenditureRequest request) {
        return expenditureService.create(request);
    }
}
