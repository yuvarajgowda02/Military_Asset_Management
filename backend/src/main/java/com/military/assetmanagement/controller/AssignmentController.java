package com.military.assetmanagement.controller;

import com.military.assetmanagement.dto.request.AssignmentRequest;
import com.military.assetmanagement.entity.Assignment;
import com.military.assetmanagement.service.AssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/assignments")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','BASE_COMMANDER')")
public class AssignmentController {

    private final AssignmentService assignmentService;

    @GetMapping
    public List<Assignment> search(
            @RequestParam(required = false) Long baseId,
            @RequestParam(required = false) Long equipmentTypeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return assignmentService.search(baseId, equipmentTypeId, startDate, endDate);
    }

    @PostMapping
    public Assignment create(@Valid @RequestBody AssignmentRequest request) {
        return assignmentService.create(request);
    }

    @PatchMapping("/{id}/return")
    public Assignment markReturned(@PathVariable Long id,
                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate returnedDate) {
        return assignmentService.markReturned(id, returnedDate);
    }
}
