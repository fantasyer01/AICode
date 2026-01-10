package com.transaction.training.controller;

import com.transaction.training.entity.TransactionLog;
import com.transaction.training.repository.*;
import com.transaction.training.service.DatabaseManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/demo")
@RequiredArgsConstructor
@Tag(name = "Utility APIs", description = "Utility operations for demo management")
@CrossOrigin(origins = "*")
public class UtilityController {
    
    private final AccountRepository accountRepository;
    private final OrderRepository orderRepository;
    private final InventoryRepository inventoryRepository;
    private final TransactionLogRepository transactionLogRepository;
    private final JdbcTemplate jdbcTemplate;
    private final DatabaseManagementService databaseManagementService;
    
    @PostMapping("/reset")
    @Operation(summary = "Reset all demo data to initial state")
    public Map<String, Object> resetData() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Clear transaction logs
            transactionLogRepository.deleteAll();
            
            // Clear demo-specific tables
            jdbcTemplate.update("DELETE FROM redo_log_simulation");
            jdbcTemplate.update("DELETE FROM undo_log_simulation");
            jdbcTemplate.update("DELETE FROM mvcc_version_chain");
            
            // Reset accounts to initial values (re-run sample data inserts)
            jdbcTemplate.update("UPDATE account SET balance = 1000.00, version = 0 WHERE account_id = 1");
            jdbcTemplate.update("UPDATE account SET balance = 2000.00, version = 0 WHERE account_id = 2");
            jdbcTemplate.update("UPDATE account SET balance = 1500.00, version = 0 WHERE account_id = 3");
            jdbcTemplate.update("UPDATE account SET balance = 3000.00, version = 0 WHERE account_id = 4");
            jdbcTemplate.update("UPDATE account SET balance = 500.00, version = 0 WHERE account_id = 5");
            
            // Reset inventory
            jdbcTemplate.update("UPDATE inventory SET quantity = 50, version = 0 WHERE product_name = 'Laptop'");
            jdbcTemplate.update("UPDATE inventory SET quantity = 200, version = 0 WHERE product_name = 'Mouse'");
            jdbcTemplate.update("UPDATE inventory SET quantity = 150, version = 0 WHERE product_name = 'Keyboard'");
            
            result.put("success", true);
            result.put("message", "Demo data reset successfully");
            result.put("accountsReset", 5);
            result.put("inventoryReset", 3);
            result.put("logsCleared", true);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    @GetMapping("/logs")
    @Operation(summary = "Get recent transaction execution logs")
    public Map<String, Object> getLogs(@RequestParam(defaultValue = "50") int limit) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<TransactionLog> logs = transactionLogRepository.findTop50ByOrderByExecutedAtDesc();
            
            result.put("success", true);
            result.put("logs", logs);
            result.put("count", logs.size());
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    @GetMapping("/code/{scenario}")
    @Operation(summary = "Get code snippet for specific scenario")
    public Map<String, Object> getCodeSnippet(@PathVariable String scenario) {
        Map<String, Object> result = new HashMap<>();
        Map<String, String> codeSnippets = getCodeSnippetLibrary();
        
        if (codeSnippets.containsKey(scenario)) {
            result.put("success", true);
            result.put("scenario", scenario);
            result.put("code", codeSnippets.get(scenario));
        } else {
            result.put("success", false);
            result.put("message", "Code snippet not found for scenario: " + scenario);
        }
        
        return result;
    }
    
    @GetMapping("/stats")
    @Operation(summary = "Get demonstration statistics")
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            long accountCount = accountRepository.count();
            long orderCount = orderRepository.count();
            long inventoryCount = inventoryRepository.count();
            long logCount = transactionLogRepository.count();
            
            // Count logs by type
            Map<String, Long> logsByType = new HashMap<>();
            jdbcTemplate.query(
                "SELECT transaction_type, COUNT(*) as count FROM transaction_log GROUP BY transaction_type",
                rs -> {
                    logsByType.put(rs.getString("transaction_type"), rs.getLong("count"));
                }
            );
            
            stats.put("totalAccounts", accountCount);
            stats.put("totalOrders", orderCount);
            stats.put("totalInventoryItems", inventoryCount);
            stats.put("totalExecutions", logCount);
            stats.put("executionsByType", logsByType);
            stats.put("databaseInfo", databaseManagementService.getActiveDatabaseInfo());
            
        } catch (Exception e) {
            stats.put("error", e.getMessage());
        }
        
        return stats;
    }
    
    private Map<String, String> getCodeSnippetLibrary() {
        Map<String, String> snippets = new HashMap<>();
        
        snippets.put("propagation-required", """
            @Transactional(propagation = Propagation.REQUIRED)
            public void requiredMethod() {
                // Joins existing transaction or creates new one
                accountRepository.save(account);
            }
            """);
        
        snippets.put("propagation-requires-new", """
            @Transactional(propagation = Propagation.REQUIRES_NEW)
            public void requiresNewMethod() {
                // Always creates new independent transaction
                logRepository.save(log);
            }
            """);
        
        snippets.put("isolation-read-committed", """
            @Transactional(isolation = Isolation.READ_COMMITTED)
            public void readCommittedMethod() {
                // Prevents dirty reads, allows non-repeatable reads
                Account account = accountRepository.findById(1L).get();
            }
            """);
        
        snippets.put("isolation-repeatable-read", """
            @Transactional(isolation = Isolation.REPEATABLE_READ)
            public void repeatableReadMethod() {
                // Prevents dirty and non-repeatable reads
                // MySQL InnoDB default isolation level
                List<Account> accounts = accountRepository.findAll();
            }
            """);
        
        snippets.put("rollback-for", """
            @Transactional(rollbackFor = Exception.class)
            public void rollbackForMethod() throws Exception {
                // Rollback on any exception including checked
                accountRepository.save(account);
                throw new IOException("Checked exception causes rollback");
            }
            """);
        
        snippets.put("pessimistic-lock", """
            @Transactional
            public void pessimisticLockMethod() {
                // Acquire exclusive lock immediately
                Account account = accountRepository.findByIdWithLock(1L).get();
                account.setBalance(newBalance);
                accountRepository.save(account);
            }
            """);
        
        return snippets;
    }
}
