package com.example.auditdemo.audit.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Container for audit data passed through AuditContext
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditData {

    /**
     * List of table operations to be audited
     */
    @Builder.Default
    private List<TableOperation> tables = new ArrayList<>();

    /**
     * Remark or description for this audit request
     */
    private String remark;

    /**
     * Flag to skip audit for this operation
     */
    @Builder.Default
    private boolean skip = false;

    /**
     * Add a single table operation
     */
    public void addTableOperation(TableOperation operation) {
        if (this.tables == null) {
            this.tables = new ArrayList<>();
        }
        this.tables.add(operation);
    }
}
