package com.transaction.training.service;

import com.transaction.training.dto.DemoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseInternalsService {
    
    private final JdbcTemplate jdbcTemplate;
    
    /**
     * Demonstrate Redo Log mechanism
     */
    @Transactional
    public DemoResponse demonstrateRedoLog() {
        DemoResponse response = DemoResponse.success("REDO_LOG_DEMO",
            "Redo Log (Write-Ahead Log) ensures durability. Changes are logged before being written to data files.");
        
        try {
            String txId = UUID.randomUUID().toString().substring(0, 8);
            
            // Simulate redo log entries
            jdbcTemplate.update(
                "INSERT INTO redo_log_simulation (transaction_id, operation_type, table_name, before_image, after_image) VALUES (?, ?, ?, ?, ?)",
                txId, "UPDATE", "account", "{\"balance\": 1000}", "{\"balance\": 1100}"
            );
            
            List<Map<String, Object>> logs = jdbcTemplate.queryForList(
                "SELECT * FROM redo_log_simulation WHERE transaction_id = ? ORDER BY log_time DESC LIMIT 5",
                txId
            );
            
            response.setResults(Map.of("redoLogs", logs, "transactionId", txId));
            response.setLogs(List.of(
                "1. Transaction begins",
                "2. Generate redo log entry with LSN (Log Sequence Number)",
                "3. Write redo log to disk (WAL - Write-Ahead Logging)",
                "4. Modify data in buffer pool",
                "5. Transaction commits",
                "6. Eventually flush dirty pages to disk",
                "Recovery: Replay redo logs to restore committed transactions"
            ));
            
            response.setCodeSnippet("""
                // Redo Log Process
                BEGIN TRANSACTION;
                
                -- Step 1: Write to redo log FIRST (WAL principle)
                INSERT INTO redo_log (lsn, tx_id, operation, table, old_value, new_value)
                VALUES (12345, 'TX001', 'UPDATE', 'account', '1000', '1100');
                
                -- Step 2: Modify data in memory
                UPDATE account SET balance = 1100 WHERE id = 1;
                
                COMMIT;
                
                -- On crash recovery:
                -- 1. Read redo log from last checkpoint
                -- 2. Replay all committed transactions
                -- 3. Restore database to consistent state
                """);
            
        } catch (Exception e) {
            log.error("Redo log demo error", e);
            response.setSuccess(false);
        }
        
        return response;
    }
    
    /**
     * Demonstrate Undo Log mechanism
     */
    @Transactional
    public DemoResponse demonstrateUndoLog() {
        DemoResponse response = DemoResponse.success("UNDO_LOG_DEMO",
            "Undo Log enables transaction rollback and provides old versions for MVCC read consistency.");
        
        try {
            String txId = UUID.randomUUID().toString().substring(0, 8);
            
            // Simulate undo log entries
            jdbcTemplate.update(
                "INSERT INTO undo_log_simulation (transaction_id, rollback_sql, rollback_data) VALUES (?, ?, ?)",
                txId,
                "UPDATE account SET balance = 1000 WHERE account_id = 1",
                "{\"account_id\": 1, \"old_balance\": 1000, \"new_balance\": 1100}"
            );
            
            List<Map<String, Object>> logs = jdbcTemplate.queryForList(
                "SELECT * FROM undo_log_simulation WHERE transaction_id = ? ORDER BY created_at DESC LIMIT 5",
                txId
            );
            
            response.setResults(Map.of("undoLogs", logs, "transactionId", txId));
            response.setLogs(List.of(
                "1. Before modifying data, save old value to undo log",
                "2. Undo log records: transaction ID, old value, operation",
                "3. On ROLLBACK: Execute reverse operations from undo log",
                "4. For MVCC: Other transactions read old versions from undo log",
                "5. Purge thread: Clean up undo logs of committed transactions",
                "6. Undo logs form version chain for snapshot reads"
            ));
            
            response.setCodeSnippet("""
                BEGIN TRANSACTION;
                
                -- Before UPDATE, create undo log
                INSERT INTO undo_log (tx_id, table, row_id, old_value)
                VALUES ('TX001', 'account', 1, '{"balance": 1000}');
                
                -- Perform UPDATE
                UPDATE account SET balance = 1100 WHERE id = 1;
                
                -- On ROLLBACK:
                SELECT old_value FROM undo_log WHERE tx_id = 'TX001';
                UPDATE account SET balance = 1000 WHERE id = 1; -- Restore
                
                -- For MVCC (another transaction reading):
                -- Read from undo log to get snapshot at their transaction start time
                SELECT * FROM undo_log WHERE tx_id < reader_tx_id;
                """);
            
        } catch (Exception e) {
            log.error("Undo log demo error", e);
            response.setSuccess(false);
        }
        
        return response;
    }
    
    /**
     * Demonstrate MVCC mechanism
     */
    @Transactional
    public DemoResponse demonstrateMVCC() {
        DemoResponse response = DemoResponse.success("MVCC_DEMO",
            "MVCC (Multi-Version Concurrency Control) allows concurrent reads and writes without locking using version chains.");
        
        try {
            String txId = UUID.randomUUID().toString().substring(0, 8);
            Long rowId = 1L;
            
            // Create version chain
            for (int version = 0; version < 3; version++) {
                jdbcTemplate.update(
                    "INSERT INTO mvcc_version_chain (row_id, transaction_id, data_snapshot, created_at) VALUES (?, ?, ?, ?)",
                    rowId,
                    txId + "-v" + version,
                    String.format("{\"balance\": %d, \"version\": %d}", 1000 + (version * 100), version),
                    LocalDateTime.now().minusSeconds(10 - version)
                );
            }
            
            List<Map<String, Object>> versionChain = jdbcTemplate.queryForList(
                "SELECT * FROM mvcc_version_chain WHERE row_id = ? ORDER BY created_at DESC",
                rowId
            );
            
            response.setResults(Map.of(
                "rowId", rowId,
                "versionChain", versionChain,
                "versionsCount", versionChain.size()
            ));
            
            response.setLogs(List.of(
                "MVCC Version Chain Explained:",
                "1. Each row has hidden columns: DB_TRX_ID (transaction ID), DB_ROLL_PTR (undo log pointer)",
                "2. Updates create new version, link to old version via roll pointer",
                "3. Read View: Transaction sees snapshot based on its start time",
                "4. Read algorithm: Traverse version chain until finding visible version",
                "5. No read locks needed - readers never block writers",
                "6. Snapshot Isolation: Consistent read without locking"
            ));
            
            response.setCodeSnippet("""
                -- Transaction T1 (started at time 100)
                BEGIN; -- T1 creates Read View at time 100
                SELECT * FROM account WHERE id = 1;
                -- Sees version with DB_TRX_ID < 100
                
                -- Transaction T2 (started at time 150)
                BEGIN;
                UPDATE account SET balance = 1500 WHERE id = 1;
                -- Creates new version with DB_TRX_ID = 150
                -- Old version still accessible via undo log
                COMMIT; -- Time 160
                
                -- Back to T1 (still at Read View time 100)
                SELECT * FROM account WHERE id = 1;
                -- Still sees old version (balance = 1000)
                -- T2's changes invisible to T1 (Repeatable Read)
                
                COMMIT;
                
                -- Version Chain Structure:
                -- Current: [ID=1, balance=1500, TRX_ID=150, ROLL_PTR→V1]
                -- V1:      [ID=1, balance=1000, TRX_ID=100, ROLL_PTR→V0]
                -- V0:      [ID=1, balance=900,  TRX_ID=50,  ROLL_PTR→NULL]
                """);
            
        } catch (Exception e) {
            log.error("MVCC demo error", e);
            response.setSuccess(false);
        }
        
        return response;
    }
    
    /**
     * Demonstrate WAL (Write-Ahead Logging) protocol
     */
    public DemoResponse demonstrateWAL() {
        DemoResponse response = DemoResponse.success("WAL_PROTOCOL",
            "Write-Ahead Logging (WAL) ensures durability: log entries must be written to disk before data modifications.");
        
        response.setLogs(List.of(
            "WAL Protocol Rules:",
            "1. Before writing data page to disk, write corresponding log to disk",
            "2. Log must be flushed before transaction commits",
            "3. Guarantees: Can reconstruct committed transactions after crash",
            "",
            "WAL Benefits:",
            "- Sequential writes to log (fast)",
            "- Random writes to data files can be delayed",
            "- Reduces disk I/O significantly",
            "- Enables crash recovery",
            "",
            "Checkpoint Process:",
            "1. Flush all dirty pages up to certain LSN",
            "2. Write checkpoint record to log",
            "3. Recovery only needs to read log from last checkpoint"
        ));
        
        response.setCodeSnippet("""
            // WAL Implementation Pseudocode
            
            function updateRecord(record, newValue) {
                // Step 1: Generate log entry
                logEntry = {
                    LSN: nextLSN++,
                    transactionId: currentTx.id,
                    type: "UPDATE",
                    table: record.table,
                    rowId: record.id,
                    oldValue: record.value,
                    newValue: newValue
                };
                
                // Step 2: Write log to disk FIRST (WAL rule)
                writeLogToDisk(logEntry);
                flushLog(); // Force to disk
                
                // Step 3: Modify data in buffer pool (memory)
                record.value = newValue;
                markPageDirty(record.page);
                
                // Step 4: Eventually flush dirty page to disk
                // (can be delayed, log protects us)
            }
            
            function commit() {
                // Write commit record to log
                commitLog = { LSN: nextLSN++, type: "COMMIT", txId: currentTx.id };
                writeLogToDisk(commitLog);
                flushLog(); // MUST flush before returning
                
                // Now transaction is durable
                // Dirty pages can be flushed later
            }
            
            function recover() {
                lastCheckpoint = findLastCheckpoint();
                
                // Redo phase: Replay all operations from checkpoint
                for (logEntry in logFile.from(lastCheckpoint)) {
                    if (logEntry.type == "UPDATE") {
                        applyUpdate(logEntry);
                    }
                }
                
                // Undo phase: Rollback uncommitted transactions
                for (tx in activeTransactions) {
                    rollbackTransaction(tx);
                }
            }
            """);
        
        return response;
    }
    
    /**
     * Demonstrate lock mechanisms
     */
    public DemoResponse demonstrateLocks() {
        DemoResponse response = DemoResponse.success("LOCK_MECHANISMS",
            "Database locks control concurrent access. Understanding lock types and granularity is crucial for performance.");
        
        response.setResults(Map.of(
            "lockTypes", Map.of(
                "Shared Lock (S)", "Read lock - multiple transactions can hold",
                "Exclusive Lock (X)", "Write lock - only one transaction can hold",
                "Intent Shared (IS)", "Intention to acquire S lock on rows",
                "Intent Exclusive (IX)", "Intention to acquire X lock on rows"
            ),
            "lockGranularity", List.of("Row-level", "Page-level", "Table-level"),
            "specialLocks", Map.of(
                "Gap Lock", "Lock on gap between index records (prevents phantom reads)",
                "Next-Key Lock", "Combination of record lock + gap lock",
                "Insert Intention Lock", "Used before inserting a row"
            )
        ));
        
        response.setCodeSnippet("""
            -- Lock Demonstration Examples
            
            -- Example 1: SELECT FOR UPDATE (Exclusive Lock)
            BEGIN;
            SELECT * FROM account WHERE id = 1 FOR UPDATE;
            -- Acquires X lock on row, blocks other updates
            UPDATE account SET balance = 1500 WHERE id = 1;
            COMMIT;
            
            -- Example 2: SELECT FOR SHARE (Shared Lock)
            BEGIN;
            SELECT * FROM account WHERE id = 1 FOR SHARE;
            -- Acquires S lock, allows other reads, blocks writes
            COMMIT;
            
            -- Example 3: Gap Lock (REPEATABLE READ isolation)
            BEGIN;
            SELECT * FROM account WHERE id BETWEEN 10 AND 20 FOR UPDATE;
            -- Locks not just existing rows, but gaps between them
            -- Prevents: INSERT INTO account (id) VALUES (15);
            -- (another transaction would block)
            COMMIT;
            
            -- Example 4: Deadlock Scenario
            -- Transaction 1:
            BEGIN;
            UPDATE account SET balance = 100 WHERE id = 1; -- Locks row 1
            -- Wait...
            UPDATE account SET balance = 200 WHERE id = 2; -- Waits for row 2
            
            -- Transaction 2 (simultaneously):
            BEGIN;
            UPDATE account SET balance = 300 WHERE id = 2; -- Locks row 2
            -- Wait...
            UPDATE account SET balance = 400 WHERE id = 1; -- Waits for row 1
            -- DEADLOCK! Database detects and kills one transaction
            
            -- Deadlock Prevention:
            -- 1. Always acquire locks in same order
            -- 2. Keep transactions short
            -- 3. Use lower isolation levels when possible
            -- 4. Implement retry logic for deadlock victims
            """);
        
        return response;
    }
}
