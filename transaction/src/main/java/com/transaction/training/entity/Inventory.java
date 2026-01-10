package com.transaction.training.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "inventory")
public class Inventory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_id")
    private Long inventoryId;
    
    @Column(name = "product_name", nullable = false, unique = true, length = 200)
    private String productName;
    
    @Column(name = "quantity", nullable = false)
    private Integer quantity = 0;
    
    @Version
    @Column(name = "version", nullable = false)
    private Integer version = 0;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
