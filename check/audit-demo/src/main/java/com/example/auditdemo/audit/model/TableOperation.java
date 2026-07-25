package com.example.auditdemo.audit.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Represents a single table operation to be audited
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableOperation {

    /**
     * Table name
     */
    private String tableName;

    /**
     * Operation type: INSERT, UPDATE, DELETE
     */
    private OperationType operation;

    /**
     * Primary key field and value (for UPDATE and DELETE)
     */
    private Map<String, Object> primaryKey;

    /**
     * Data before the operation (for UPDATE and DELETE)
     */
    private Map<String, Object> beforeData;

    /**
     * Data after the operation (for INSERT and UPDATE)
     */
    private Map<String, Object> afterData;

    /**
     * Parent table reference for multi-table operations
     * Format: "parent_table.id" 
     * This indicates that this table's foreign key should reference the parent table's generated ID
     */
    private String parentRef;

    /**
     * Foreign key field name that references the parent table
     */
    private String foreignKeyField;
}
