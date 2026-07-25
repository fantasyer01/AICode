package com.example.auditdemo.audit.executor;

import com.example.auditdemo.audit.model.OperationType;
import com.example.auditdemo.audit.model.TableOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Generic SQL executor that executes table operations from audit snapshots.
 * Supports INSERT, UPDATE, DELETE operations on any table.
 */
@Component
@Slf4j
public class GenericSqlExecutor {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    /**
     * Execute a list of table operations in order.
     * Handles parent-child relationships by storing generated keys.
     *
     * @param operations List of table operations
     * @return Map of generated keys (table.id -> value)
     */
    public Map<String, Long> executeOperations(List<TableOperation> operations) {
        Map<String, Long> generatedKeys = new HashMap<>();

        for (TableOperation op : operations) {
            Long generatedKey = executeOperation(op, generatedKeys);
            if (generatedKey != null) {
                generatedKeys.put(op.getTableName() + ".id", generatedKey);
            }
        }

        return generatedKeys;
    }

    /**
     * Execute a single table operation
     *
     * @param op            Table operation
     * @param generatedKeys Map of previously generated keys
     * @return Generated key for INSERT, null otherwise
     */
    public Long executeOperation(TableOperation op, Map<String, Long> generatedKeys) {
        switch (op.getOperation()) {
            case INSERT:
                return executeInsert(op, generatedKeys);
            case UPDATE:
                executeUpdate(op);
                return null;
            case DELETE:
                executeDelete(op);
                return null;
            default:
                throw new IllegalArgumentException("Unknown operation type: " + op.getOperation());
        }
    }

    private Long executeInsert(TableOperation op, Map<String, Long> generatedKeys) {
        Map<String, Object> data = new HashMap<>(op.getAfterData());

        // Handle parent reference for multi-table operations
        if (op.getParentRef() != null && op.getForeignKeyField() != null) {
            Long parentId = generatedKeys.get(op.getParentRef());
            if (parentId != null) {
                data.put(op.getForeignKeyField(), parentId);
            }
        }

        // Build INSERT SQL
        String columns = String.join(", ", data.keySet());
        String params = data.keySet().stream()
                .map(k -> ":" + k)
                .collect(Collectors.joining(", "));

        String sql = String.format("INSERT INTO %s (%s) VALUES (%s)",
                op.getTableName(), columns, params);

        log.debug("Executing INSERT: {}", sql);
        log.debug("Parameters: {}", data);

        MapSqlParameterSource paramSource = new MapSqlParameterSource(data);
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(sql, paramSource, keyHolder);

        Number key = keyHolder.getKey();
        return key != null ? key.longValue() : null;
    }

    private void executeUpdate(TableOperation op) {
        Map<String, Object> data = op.getAfterData();
        Map<String, Object> primaryKey = op.getPrimaryKey();

        if (primaryKey == null || primaryKey.isEmpty()) {
            throw new IllegalArgumentException("Primary key is required for UPDATE operation");
        }

        // Build SET clause
        String setClause = data.keySet().stream()
                .map(k -> k + " = :" + k)
                .collect(Collectors.joining(", "));

        // Build WHERE clause
        String whereClause = primaryKey.keySet().stream()
                .map(k -> k + " = :pk_" + k)
                .collect(Collectors.joining(" AND "));

        String sql = String.format("UPDATE %s SET %s WHERE %s",
                op.getTableName(), setClause, whereClause);

        log.debug("Executing UPDATE: {}", sql);

        // Merge parameters
        Map<String, Object> params = new HashMap<>(data);
        primaryKey.forEach((k, v) -> params.put("pk_" + k, v));

        jdbcTemplate.update(sql, params);
    }

    private void executeDelete(TableOperation op) {
        Map<String, Object> primaryKey = op.getPrimaryKey();

        if (primaryKey == null || primaryKey.isEmpty()) {
            throw new IllegalArgumentException("Primary key is required for DELETE operation");
        }

        // Build WHERE clause
        String whereClause = primaryKey.keySet().stream()
                .map(k -> k + " = :" + k)
                .collect(Collectors.joining(" AND "));

        String sql = String.format("DELETE FROM %s WHERE %s",
                op.getTableName(), whereClause);

        log.debug("Executing DELETE: {}", sql);

        jdbcTemplate.update(sql, primaryKey);
    }
}
