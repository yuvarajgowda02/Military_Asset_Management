package com.military.assetmanagement.controller;

import com.military.assetmanagement.dto.response.DashboardResponse;
import com.military.assetmanagement.dto.response.MovementDetailResponse;
import com.military.assetmanagement.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','BASE_COMMANDER')")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/metrics")
    public DashboardResponse metrics(
            @RequestParam(required = false) Long baseId,
            @RequestParam(required = false) Long equipmentTypeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return dashboardService.getMetrics(baseId, equipmentTypeId, startDate, endDate);
    }

    @GetMapping("/movement-details")
    public MovementDetailResponse movementDetails(
            @RequestParam(required = false) Long baseId,
            @RequestParam(required = false) Long equipmentTypeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return dashboardService.getMovementDetails(baseId, equipmentTypeId, startDate, endDate);
    }
}
