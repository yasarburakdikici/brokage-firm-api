package com.brokage.challenge.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String operation;
    
    @Column(nullable = false)
    private String entityType;
    
    @Column
    private Long entityId;
    
    @Column
    private String customerId;
    
    @Column
    private String details;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditStatus status;
    
    @Column(nullable = false, updatable = false)
    private Instant timestamp;
    
    @Column
    private String errorMessage;
}
