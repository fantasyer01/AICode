package com.transaction.training.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "transaction_log")
public class TransactionLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;
    
    @Column(name = "transaction_type", nullable = false, length = 100)
    private String transactionType;
    
    @Column(name = "operation", nullable = false, length = 200)
    private String operation;
    
    @Column(name = "status", nullable = false, length = 20)
    private String status;
    
    @Column(name = "details", columnDefinition = "TEXT")
    private String details;
    
    @Column(name = "executed_at")
    private LocalDateTime executedAt;
    
    @PrePersist
    protected void onCreate() {
        executedAt = LocalDateTime.now();
    }
}
