package com.military.assetmanagement.service;

import com.military.assetmanagement.entity.AuditLog;
import com.military.assetmanagement.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public List<AuditLog> recent(int limit) {
        return auditLogRepository.findAll(PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "timestamp")))
                .getContent();
    }
}
