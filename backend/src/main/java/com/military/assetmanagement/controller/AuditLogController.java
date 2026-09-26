package com.military.assetmanagement.controller;

import com.military.assetmanagement.entity.AuditLog;
import com.military.assetmanagement.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/audit-logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    public List<AuditLog> recent(@RequestParam(defaultValue = "200") int limit) {
        return auditLogService.recent(limit);
    }
}
