package com.military.assetmanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", length = 60)
    private String username;

    @Column(name = "user_role", length = 30)
    private String userRole;

    @Column(nullable = false, length = 20)
    private String method;

    @Column(nullable = false, length = 250)
    private String endpoint;

    @Column(nullable = false, length = 60)
    private String action;

    @Lob
    @Column(name = "request_summary")
    private String requestSummary;

    @Column(name = "status_code")
    private Integer statusCode;

    @Column(name = "ip_address", length = 60)
    private String ipAddress;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        this.timestamp = LocalDateTime.now();
    }
}
