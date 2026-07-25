package com.example.auditdemo.audit.snapshot;

import com.example.auditdemo.audit.model.SnapshotData;

/**
 * Snapshot builder interface for different business types
 */
public interface SnapshotBuilder {
    
    /**
     * Check if this builder supports the given business type
     */
    boolean supports(String businessType);
    
    /**
     * Build snapshot data from method arguments
     * 
     * @param operationType Operation type (INSERT, UPDATE, DELETE)
     * @param args Method arguments
     * @return Snapshot data for display
     */
    SnapshotData buildSnapshot(String operationType, Object[] args);
}
