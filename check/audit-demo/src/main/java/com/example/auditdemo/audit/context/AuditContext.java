package com.example.auditdemo.audit.context;

import com.example.auditdemo.audit.model.AuditData;
import com.example.auditdemo.audit.model.OperationType;
import com.example.auditdemo.audit.model.TableOperation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ThreadLocal-based context for passing audit data from business code to AOP aspect.
 * 
 * Usage in Service methods:
 * <pre>
 * {@code
 * @AuditRequired(businessType = "USER_MGMT", desc = "Update User")
 * public void updateUser(UserDTO dto) {
 *     User oldUser = userMapper.selectById(dto.getId());
 *     AuditContext.setSingleTable("user_info", OperationType.UPDATE,
 *         Map.of("id", dto.getId()), 
 *         convertToMap(oldUser), 
 *         convertToMap(dto), 
 *         "Update user information");
 * }
 * }
 * </pre>
 */
public class AuditContext {

    private static final ThreadLocal<AuditData> CONTEXT = new ThreadLocal<>();

    /**
     * Set audit data for a single table operation
     *
     * @param tableName   Table name
     * @param operation   Operation type
     * @param primaryKey  Primary key map (null for INSERT)
     * @param beforeData  Data before operation (null for INSERT)
     * @param afterData   Data after operation (null for DELETE)
     * @param remark      Operation remark
     */
    public static void setSingleTable(String tableName,
                                       OperationType operation,
                                       Map<String, Object> primaryKey,
                                       Map<String, Object> beforeData,
                                       Map<String, Object> afterData,
                                       String remark) {
        AuditData data = new AuditData();
        data.setRemark(remark);
        data.addTableOperation(TableOperation.builder()
                .tableName(tableName)
                .operation(operation)
                .primaryKey(primaryKey)
                .beforeData(beforeData)
                .afterData(afterData)
                .build());
        CONTEXT.set(data);
    }

    /**
     * Set audit data for multiple table operations
     *
     * @param operations List of table operations
     * @param remark     Operation remark
     */
    public static void setMultiTable(List<TableOperation> operations, String remark) {
        AuditData data = new AuditData();
        data.setRemark(remark);
        data.setTables(operations);
        CONTEXT.set(data);
    }

    /**
     * Get current audit data
     *
     * @return AuditData or null if not set
     */
    public static AuditData get() {
        return CONTEXT.get();
    }

    /**
     * Clear the context (called by AOP after processing)
     */
    public static void clear() {
        CONTEXT.remove();
    }

    /**
     * Mark this operation to skip audit.
     * Useful for emergency operations or admin overrides.
     */
    public static void skip() {
        AuditData data = CONTEXT.get();
        if (data == null) {
            data = new AuditData();
        }
        data.setSkip(true);
        CONTEXT.set(data);
    }

    /**
     * Check if audit data is set
     */
    public static boolean hasData() {
        return CONTEXT.get() != null && !CONTEXT.get().isSkip();
    }

    /**
     * Builder helper for creating multi-table operations
     */
    public static MultiTableBuilder multiTable() {
        return new MultiTableBuilder();
    }

    /**
     * Builder class for multi-table operations
     */
    public static class MultiTableBuilder {
        private final List<TableOperation> operations = new ArrayList<>();
        private String remark;

        public MultiTableBuilder addTable(TableOperation operation) {
            operations.add(operation);
            return this;
        }

        public MultiTableBuilder addInsert(String tableName, Map<String, Object> afterData) {
            operations.add(TableOperation.builder()
                    .tableName(tableName)
                    .operation(OperationType.INSERT)
                    .afterData(afterData)
                    .build());
            return this;
        }

        public MultiTableBuilder addInsertWithParent(String tableName, 
                                                      Map<String, Object> afterData,
                                                      String parentRef,
                                                      String foreignKeyField) {
            operations.add(TableOperation.builder()
                    .tableName(tableName)
                    .operation(OperationType.INSERT)
                    .afterData(afterData)
                    .parentRef(parentRef)
                    .foreignKeyField(foreignKeyField)
                    .build());
            return this;
        }

        public MultiTableBuilder addUpdate(String tableName,
                                           Map<String, Object> primaryKey,
                                           Map<String, Object> beforeData,
                                           Map<String, Object> afterData) {
            operations.add(TableOperation.builder()
                    .tableName(tableName)
                    .operation(OperationType.UPDATE)
                    .primaryKey(primaryKey)
                    .beforeData(beforeData)
                    .afterData(afterData)
                    .build());
            return this;
        }

        public MultiTableBuilder addDelete(String tableName,
                                           Map<String, Object> primaryKey,
                                           Map<String, Object> beforeData) {
            operations.add(TableOperation.builder()
                    .tableName(tableName)
                    .operation(OperationType.DELETE)
                    .primaryKey(primaryKey)
                    .beforeData(beforeData)
                    .build());
            return this;
        }

        public MultiTableBuilder remark(String remark) {
            this.remark = remark;
            return this;
        }

        public void submit() {
            setMultiTable(operations, remark);
        }
    }
}
