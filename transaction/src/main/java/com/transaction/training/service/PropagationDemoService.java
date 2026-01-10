package com.transaction.training.service;

import com.transaction.training.dto.DemoResponse;
import com.transaction.training.entity.Account;
import com.transaction.training.entity.TransactionLog;
import com.transaction.training.repository.AccountRepository;
import com.transaction.training.repository.TransactionLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PropagationDemoService {
    
    private final AccountRepository accountRepository;
    private final TransactionLogRepository transactionLogRepository;
    
    /**
     * Demo: REQUIRED propagation (default)
     * If a transaction exists, join it; otherwise, create a new one
     */
    public DemoResponse demonstrateRequired() {
        DemoResponse response = DemoResponse.success("PROPAGATION_REQUIRED", 
            "REQUIRED is the default propagation. Method will join existing transaction or create new one.");
        
        List<DemoResponse.DemoStep> steps = new ArrayList<>();
        Map<String, Object> before = new HashMap<>();
        Map<String, Object> after = new HashMap<>();
        
        try {
            // Get initial state
            Account account = accountRepository.findById(1L).orElseThrow();
            before.put("accountBalance", account.getBalance());
            
            steps.add(DemoResponse.DemoStep.builder()
                    .stepNumber(1)
                    .description("Start outer transaction with REQUIRED propagation")
                    .status("STARTED")
                    .build());
            
            // Call transactional method
            requiredOuterMethod(1L, new BigDecimal("100.00"));
            
            // Get final state
            account = accountRepository.findById(1L).orElseThrow();
            after.put("accountBalance", account.getBalance());
            
            steps.add(DemoResponse.DemoStep.builder()
                    .stepNumber(2)
                    .description("Both outer and inner methods executed in same transaction")
                    .status("SUCCESS")
                    .details("Transaction committed successfully")
                    .build());
            
            response.setSteps(steps);
            response.setDatabaseState(DemoResponse.DatabaseState.builder()
                    .before(before)
                    .after(after)
                    .build());
            response.setLogs(List.of(
                "Outer method creates transaction T1",
                "Inner method joins transaction T1",
                "Both operations committed together"
            ));
            
            response.setCodeSnippet("""
                @Transactional(propagation = Propagation.REQUIRED)
                public void outerMethod() {
                    // Modify account
                    innerMethod(); // Joins same transaction
                }
                
                @Transactional(propagation = Propagation.REQUIRED)
                public void innerMethod() {
                    // Logs transaction - same transaction as outer
                }
                """);
            
        } catch (Exception e) {
            log.error("Error in REQUIRED demo", e);
            response.setSuccess(false);
            response.setExplanation("Error: " + e.getMessage());
        }
        
        return response;
    }
    
    @Transactional(propagation = Propagation.REQUIRED)
    public void requiredOuterMethod(Long accountId, BigDecimal amount) {
        Account account = accountRepository.findById(accountId).orElseThrow();
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);
        
        log.info("Outer method: Updated account {} balance to {}", accountId, account.getBalance());
        
        // Call inner method - will join same transaction
        requiredInnerMethod("REQUIRED_DEMO", "Outer method execution");
    }
    
    @Transactional(propagation = Propagation.REQUIRED)
    public void requiredInnerMethod(String type, String operation) {
        TransactionLog txLog = new TransactionLog();
        txLog.setTransactionType(type);
        txLog.setOperation(operation);
        txLog.setStatus("SUCCESS");
        txLog.setDetails("Inner method joined existing transaction");
        transactionLogRepository.save(txLog);
        
        log.info("Inner method: Logged transaction");
    }
    
    /**
     * Demo: REQUIRES_NEW propagation
     * Always create a new transaction, suspending current one if exists
     */
    public DemoResponse demonstrateRequiresNew() {
        DemoResponse response = DemoResponse.success("PROPAGATION_REQUIRES_NEW",
            "REQUIRES_NEW always creates a new independent transaction, suspending any existing transaction.");
        
        List<DemoResponse.DemoStep> steps = new ArrayList<>();
        
        try {
            steps.add(DemoResponse.DemoStep.builder()
                    .stepNumber(1)
                    .description("Start outer transaction T1")
                    .status("STARTED")
                    .build());
            
            requiresNewOuterMethod(1L, new BigDecimal("50.00"));
            
            steps.add(DemoResponse.DemoStep.builder()
                    .stepNumber(2)
                    .description("Inner method created new transaction T2, suspended T1")
                    .status("SUCCESS")
                    .details("T2 committed independently, then T1 resumed and committed")
                    .build());
            
            response.setSteps(steps);
            response.setLogs(List.of(
                "Transaction T1 started in outer method",
                "Transaction T1 suspended",
                "Transaction T2 created in inner method",
                "Transaction T2 committed",
                "Transaction T1 resumed",
                "Transaction T1 committed"
            ));
            
            response.setCodeSnippet("""
                @Transactional(propagation = Propagation.REQUIRED)
                public void outerMethod() {
                    // In transaction T1
                    innerMethod(); // Creates new T2, suspends T1
                    // T1 resumed after T2 completes
                }
                
                @Transactional(propagation = Propagation.REQUIRES_NEW)
                public void innerMethod() {
                    // In new transaction T2
                    // If T2 rolls back, T1 is not affected
                }
                """);
            
        } catch (Exception e) {
            log.error("Error in REQUIRES_NEW demo", e);
            response.setSuccess(false);
            response.setExplanation("Error: " + e.getMessage());
        }
        
        return response;
    }
    
    @Transactional(propagation = Propagation.REQUIRED)
    public void requiresNewOuterMethod(Long accountId, BigDecimal amount) {
        Account account = accountRepository.findById(accountId).orElseThrow();
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);
        
        log.info("Outer transaction: Modified account");
        
        // This will create a new independent transaction
        requiresNewInnerMethod("REQUIRES_NEW_DEMO", "Inner method with new transaction");
        
        log.info("Outer transaction: Resumed after inner completed");
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void requiresNewInnerMethod(String type, String operation) {
        TransactionLog txLog = new TransactionLog();
        txLog.setTransactionType(type);
        txLog.setOperation(operation);
        txLog.setStatus("SUCCESS");
        txLog.setDetails("This executes in a completely new transaction");
        transactionLogRepository.save(txLog);
        
        log.info("New transaction: Logged and will commit independently");
    }
    
    /**
     * Demo: NESTED propagation
     * Execute within a nested transaction if current transaction exists
     */
    public DemoResponse demonstrateNested() {
        DemoResponse response = DemoResponse.success("PROPAGATION_NESTED",
            "NESTED creates a savepoint within existing transaction. Can rollback to savepoint without affecting outer transaction.");
        
        List<DemoResponse.DemoStep> steps = new ArrayList<>();
        
        try {
            steps.add(DemoResponse.DemoStep.builder()
                    .stepNumber(1)
                    .description("Start outer transaction with savepoint capability")
                    .status("STARTED")
                    .build());
            
            nestedOuterMethod(1L, new BigDecimal("75.00"));
            
            steps.add(DemoResponse.DemoStep.builder()
                    .stepNumber(2)
                    .description("Inner method executed within nested transaction (savepoint)")
                    .status("SUCCESS")
                    .details("Can rollback inner without affecting outer")
                    .build());
            
            response.setSteps(steps);
            response.setLogs(List.of(
                "Outer transaction T1 started",
                "Savepoint S1 created for nested transaction",
                "Nested transaction operations",
                "If nested fails, rollback to S1 only",
                "Outer transaction T1 commits normally"
            ));
            
            response.setCodeSnippet("""
                @Transactional(propagation = Propagation.REQUIRED)
                public void outerMethod() {
                    // Operation 1 in T1
                    try {
                        nestedMethod(); // Creates savepoint
                    } catch (Exception e) {
                        // Rollback to savepoint, T1 continues
                    }
                    // Operation 2 in T1 - still committed
                }
                
                @Transactional(propagation = Propagation.NESTED)
                public void nestedMethod() {
                    // Operations within savepoint
                    // Can be rolled back independently
                }
                """);
            
        } catch (Exception e) {
            log.error("Error in NESTED demo", e);
            response.setSuccess(false);
        }
        
        return response;
    }
    
    @Transactional(propagation = Propagation.REQUIRED)
    public void nestedOuterMethod(Long accountId, BigDecimal amount) {
        Account account = accountRepository.findById(accountId).orElseThrow();
        BigDecimal originalBalance = account.getBalance();
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);
        
        log.info("Outer: Balance updated from {} to {}", originalBalance, account.getBalance());
        
        try {
            nestedInnerMethod("NESTED_DEMO", "Nested operation");
        } catch (Exception e) {
            log.warn("Nested transaction failed, but outer continues", e);
        }
    }
    
    @Transactional(propagation = Propagation.NESTED)
    public void nestedInnerMethod(String type, String operation) {
        TransactionLog txLog = new TransactionLog();
        txLog.setTransactionType(type);
        txLog.setOperation(operation);
        txLog.setStatus("SUCCESS");
        txLog.setDetails("Nested transaction with savepoint");
        transactionLogRepository.save(txLog);
        
        log.info("Nested: Logged within savepoint");
    }
    
    /**
     * Demo: SUPPORTS propagation
     * Execute within transaction if one exists, otherwise execute non-transactionally
     */
    public DemoResponse demonstrateSupports() {
        DemoResponse response = DemoResponse.success("PROPAGATION_SUPPORTS",
            "SUPPORTS will participate in transaction if one exists, otherwise executes non-transactionally.");
        
        List<DemoResponse.DemoStep> steps = new ArrayList<>();
        List<String> logs = new ArrayList<>();
        Map<String, Object> results = new HashMap<>();
        
        try {
            steps.add(DemoResponse.DemoStep.builder()
                    .stepNumber(1)
                    .description("Case 1: Call SUPPORTS within transaction context")
                    .status("STARTED")
                    .build());
            
            // Case 1: With transaction
            supportsOuterWithTransaction(1L, "SUPPORTS_DEMO_WITH_TX", "Called within transaction");
            
            logs.add("Case 1: Outer transaction T1 started");
            logs.add("SUPPORTS method joins transaction T1");
            logs.add("Both operations committed together in T1");
            
            steps.add(DemoResponse.DemoStep.builder()
                    .stepNumber(2)
                    .description("Case 2: Call SUPPORTS without transaction context")
                    .status("IN_PROGRESS")
                    .build());
            
            // Case 2: Without transaction
            supportsMethodDirect("SUPPORTS_DEMO_NO_TX", "Called without transaction");
            
            logs.add("Case 2: No outer transaction");
            logs.add("SUPPORTS method executes non-transactionally");
            logs.add("Operations auto-committed individually");
            
            steps.add(DemoResponse.DemoStep.builder()
                    .stepNumber(3)
                    .description("SUPPORTS adapts to calling context")
                    .status("SUCCESS")
                    .details("With transaction: joins it. Without transaction: executes normally.")
                    .build());
            
            results.put("behavior", "Flexible - adapts to context");
            results.put("withTransaction", "Joins existing transaction");
            results.put("withoutTransaction", "Executes non-transactionally");
            results.put("useCase", "Read operations that can work with or without transaction");
            
        } catch (Exception e) {
            log.error("Error in SUPPORTS demo", e);
            response.setSuccess(false);
            logs.add("ERROR: " + e.getMessage());
        }
        
        response.setSteps(steps);
        response.setLogs(logs);
        response.setResults(results);
        response.setCodeSnippet("""
            // Case 1: Called within transaction
            @Transactional
            public void outerMethod() {
                supportsMethod(); // Joins transaction T1
            }
            
            // Case 2: Called without transaction
            public void nonTransactionalCaller() {
                supportsMethod(); // Executes non-transactionally
            }
            
            @Transactional(propagation = Propagation.SUPPORTS)
            public void supportsMethod() {
                // Flexible execution context
                // Works with or without transaction
            }
            """);
        
        return response;
    }
    
    @Transactional(propagation = Propagation.REQUIRED)
    public void supportsOuterWithTransaction(Long accountId, String type, String operation) {
        Account account = accountRepository.findById(accountId).orElseThrow();
        BigDecimal originalBalance = account.getBalance();
        account.setBalance(account.getBalance().add(new BigDecimal("10")));
        accountRepository.save(account);
        
        log.info("Outer transaction: Modified account from {} to {}", originalBalance, account.getBalance());
        
        // Call SUPPORTS method - will join this transaction
        supportsMethod(type, operation);
    }
    
    @Transactional(propagation = Propagation.SUPPORTS)
    public void supportsMethod(String type, String operation) {
        TransactionLog txLog = new TransactionLog();
        txLog.setTransactionType(type);
        txLog.setOperation(operation);
        txLog.setStatus("SUCCESS");
        txLog.setDetails("SUPPORTS: Joined existing transaction");
        transactionLogRepository.save(txLog);
        
        log.info("SUPPORTS: Executed within transaction context");
    }
    
    @Transactional(propagation = Propagation.SUPPORTS)
    public void supportsMethodDirect(String type, String operation) {
        TransactionLog txLog = new TransactionLog();
        txLog.setTransactionType(type);
        txLog.setOperation(operation);
        txLog.setStatus("SUCCESS");
        txLog.setDetails("SUPPORTS: Executed non-transactionally");
        transactionLogRepository.save(txLog);
        
        log.info("SUPPORTS: Executed without transaction context (auto-commit)");
    }
    
    /**
     * Demo: NOT_SUPPORTED propagation
     * Always execute non-transactionally, suspending current transaction if exists
     */
    public DemoResponse demonstrateNotSupported() {
        DemoResponse response = DemoResponse.success("PROPAGATION_NOT_SUPPORTED",
            "NOT_SUPPORTED always executes non-transactionally. Suspends any existing transaction.");
        
        List<DemoResponse.DemoStep> steps = new ArrayList<>();
        List<String> logs = new ArrayList<>();
        Map<String, Object> results = new HashMap<>();
        
        try {
            steps.add(DemoResponse.DemoStep.builder()
                    .stepNumber(1)
                    .description("Start outer transaction T1")
                    .status("STARTED")
                    .build());
            
            notSupportedOuterMethod(1L, "NOT_SUPPORTED_DEMO", "Called from transaction");
            
            logs.add("Outer transaction T1 started");
            logs.add("T1 suspended before calling NOT_SUPPORTED method");
            logs.add("NOT_SUPPORTED method executes without transaction");
            logs.add("Operations auto-committed immediately");
            logs.add("T1 resumed after NOT_SUPPORTED completes");
            logs.add("T1 committed");
            
            steps.add(DemoResponse.DemoStep.builder()
                    .stepNumber(2)
                    .description("NOT_SUPPORTED method executed without transaction")
                    .status("SUCCESS")
                    .details("Transaction T1 was suspended, then resumed after execution")
                    .build());
            
            results.put("behavior", "Always non-transactional");
            results.put("transactionSuspended", true);
            results.put("useCase", "Read-only operations, reporting, logging that don't need transaction overhead");
            
        } catch (Exception e) {
            log.error("Error in NOT_SUPPORTED demo", e);
            response.setSuccess(false);
            logs.add("ERROR: " + e.getMessage());
        }
        
        response.setSteps(steps);
        response.setLogs(logs);
        response.setResults(results);
        response.setCodeSnippet("""
            @Transactional
            public void outerMethod() {
                // In transaction T1
                account.setBalance(newBalance);
                
                notSupportedMethod(); // T1 suspended, executes without transaction
                
                // T1 resumed
            }
            
            @Transactional(propagation = Propagation.NOT_SUPPORTED)
            public void notSupportedMethod() {
                // No transaction here - operations auto-commit
                // Useful for read-only operations that don't need transaction overhead
                log.info(\"Expensive logging operation\");
            }
            """);
        
        return response;
    }
    
    @Transactional(propagation = Propagation.REQUIRED)
    public void notSupportedOuterMethod(Long accountId, String type, String operation) {
        Account account = accountRepository.findById(accountId).orElseThrow();
        BigDecimal originalBalance = account.getBalance();
        account.setBalance(account.getBalance().add(new BigDecimal("20")));
        accountRepository.save(account);
        
        log.info("Outer transaction T1: Modified account from {} to {}", originalBalance, account.getBalance());
        
        // Call NOT_SUPPORTED method - will suspend T1
        notSupportedMethod(type, operation);
        
        log.info("Outer transaction T1: Resumed after NOT_SUPPORTED completed");
    }
    
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void notSupportedMethod(String type, String operation) {
        TransactionLog txLog = new TransactionLog();
        txLog.setTransactionType(type);
        txLog.setOperation(operation);
        txLog.setStatus("SUCCESS");
        txLog.setDetails("NOT_SUPPORTED: Executed without transaction (suspended outer)");
        transactionLogRepository.save(txLog);
        
        log.info("NOT_SUPPORTED: Executed non-transactionally (outer transaction suspended)");
    }
    
    /**
     * Demo: MANDATORY propagation
     * Must execute within existing transaction, throws exception if none exists
     */
    public DemoResponse demonstrateMandatory() {
        DemoResponse response = DemoResponse.success("PROPAGATION_MANDATORY",
            "MANDATORY requires existing transaction. Throws exception if called without transaction context.");
        
        List<DemoResponse.DemoStep> steps = new ArrayList<>();
        List<String> logs = new ArrayList<>();
        Map<String, Object> results = new HashMap<>();
        
        try {
            steps.add(DemoResponse.DemoStep.builder()
                    .stepNumber(1)
                    .description("Start outer transaction T1")
                    .status("STARTED")
                    .build());
            
            // Case 1: With transaction - succeeds
            mandatoryOuterMethod(1L, "MANDATORY_DEMO_SUCCESS", "Called within transaction");
            
            logs.add("Case 1: Outer transaction T1 started");
            logs.add("MANDATORY method joins transaction T1 - SUCCESS");
            logs.add("Both operations committed together");
            
            steps.add(DemoResponse.DemoStep.builder()
                    .stepNumber(2)
                    .description("MANDATORY method executed successfully within transaction")
                    .status("SUCCESS")
                    .details("Transaction context is required and was present")
                    .build());
            
            // Case 2: Without transaction - would fail
            logs.add("Case 2: If called without transaction context:");
            logs.add("MANDATORY method throws IllegalTransactionStateException");
            logs.add("Error: No existing transaction found");
            
            results.put("behavior", "Requires existing transaction");
            results.put("withTransaction", "SUCCESS - joins transaction");
            results.put("withoutTransaction", "FAILURE - throws IllegalTransactionStateException");
            results.put("useCase", "Methods that must be part of a larger transactional operation");
            
        } catch (Exception e) {
            log.error("Error in MANDATORY demo", e);
            response.setSuccess(false);
            logs.add("ERROR: " + e.getMessage());
        }
        
        response.setSteps(steps);
        response.setLogs(logs);
        response.setResults(results);
        response.setCodeSnippet("""
            // This works - transaction exists
            @Transactional
            public void outerMethod() {
                mandatoryMethod(); // OK - joins existing transaction T1
            }
            
            // This fails - no transaction
            public void noTransactionMethod() {
                mandatoryMethod(); // Throws IllegalTransactionStateException
            }
            
            @Transactional(propagation = Propagation.MANDATORY)
            public void mandatoryMethod() {
                // Must be called within transaction
                // Enforces transactional context requirement
            }
            """);
        
        return response;
    }
    
    @Transactional(propagation = Propagation.REQUIRED)
    public void mandatoryOuterMethod(Long accountId, String type, String operation) {
        Account account = accountRepository.findById(accountId).orElseThrow();
        BigDecimal originalBalance = account.getBalance();
        account.setBalance(account.getBalance().add(new BigDecimal("30")));
        accountRepository.save(account);
        
        log.info("Outer transaction T1: Modified account from {} to {}", originalBalance, account.getBalance());
        
        // Call MANDATORY method - will join this transaction
        mandatoryMethod(type, operation);
    }
    
    @Transactional(propagation = Propagation.MANDATORY)
    public void mandatoryMethod(String type, String operation) {
        TransactionLog txLog = new TransactionLog();
        txLog.setTransactionType(type);
        txLog.setOperation(operation);
        txLog.setStatus("SUCCESS");
        txLog.setDetails("MANDATORY: Joined existing transaction (required)");
        transactionLogRepository.save(txLog);
        
        log.info("MANDATORY: Executed within required transaction context");
    }
    
    /**
     * Demo: NEVER propagation
     * Must execute non-transactionally, throws exception if transaction exists
     */
    public DemoResponse demonstrateNever() {
        DemoResponse response = DemoResponse.success("PROPAGATION_NEVER",
            "NEVER must execute without transaction. Throws exception if transaction exists.");
        
        List<DemoResponse.DemoStep> steps = new ArrayList<>();
        List<String> logs = new ArrayList<>();
        Map<String, Object> results = new HashMap<>();
        
        try {
            steps.add(DemoResponse.DemoStep.builder()
                    .stepNumber(1)
                    .description("Call NEVER method without transaction context")
                    .status("STARTED")
                    .build());
            
            // Case 1: Without transaction - succeeds
            neverMethod("NEVER_DEMO_SUCCESS", "Called without transaction");
            
            logs.add("Case 1: No outer transaction");
            logs.add("NEVER method executes non-transactionally - SUCCESS");
            logs.add("Operations auto-committed");
            
            steps.add(DemoResponse.DemoStep.builder()
                    .stepNumber(2)
                    .description("NEVER method executed successfully without transaction")
                    .status("SUCCESS")
                    .details("No transaction context - execution allowed")
                    .build());
            
            // Case 2: With transaction - would fail
            logs.add("Case 2: If called within transaction context:");
            logs.add("NEVER method throws IllegalTransactionStateException");
            logs.add("Error: Existing transaction found, but NEVER requires none");
            
            results.put("behavior", "Must execute without transaction");
            results.put("withTransaction", "FAILURE - throws IllegalTransactionStateException");
            results.put("withoutTransaction", "SUCCESS - executes normally");
            results.put("useCase", "Operations that must avoid transaction overhead (e.g., monitoring)");
            
        } catch (Exception e) {
            log.error("Error in NEVER demo", e);
            response.setSuccess(false);
            logs.add("ERROR: " + e.getMessage());
        }
        
        response.setSteps(steps);
        response.setLogs(logs);
        response.setResults(results);
        response.setCodeSnippet("""
            // This works - no transaction
            public void nonTransactionalCaller() {
                neverMethod(); // OK - no transaction context
            }
            
            // This fails - transaction exists
            @Transactional
            public void transactionalMethod() {
                neverMethod(); // Throws IllegalTransactionStateException
            }
            
            @Transactional(propagation = Propagation.NEVER)
            public void neverMethod() {
                // Must be called without transaction
                // Useful for ensuring no transaction overhead
                // e.g., health checks, monitoring
            }
            """);
        
        return response;
    }
    
    @Transactional(propagation = Propagation.NEVER)
    public void neverMethod(String type, String operation) {
        TransactionLog txLog = new TransactionLog();
        txLog.setTransactionType(type);
        txLog.setOperation(operation);
        txLog.setStatus("SUCCESS");
        txLog.setDetails("NEVER: Executed without transaction (required)");
        transactionLogRepository.save(txLog);
        
        log.info("NEVER: Executed non-transactionally (no transaction allowed)");
    }
    
    /**
     * Compare all propagation behaviors
     */
    public DemoResponse comparePropagations() {
        DemoResponse response = DemoResponse.success("PROPAGATION_COMPARISON",
            "Comparison of transaction propagation behaviors and their characteristics.");
        
        Map<String, Object> comparisonTable = new HashMap<>();
        
        Map<String, String> required = new HashMap<>();
        required.put("propagation", "REQUIRED");
        required.put("transactionExists", "Join it");
        required.put("noTransaction", "Create new");
        required.put("newTransaction", "No");
        required.put("useCase", "Default behavior - most common");
        
        Map<String, String> requiresNew = new HashMap<>();
        requiresNew.put("propagation", "REQUIRES_NEW");
        requiresNew.put("transactionExists", "Suspend, create new");
        requiresNew.put("noTransaction", "Create new");
        requiresNew.put("newTransaction", "Always");
        requiresNew.put("useCase", "Independent operations (audit logging)");
        
        Map<String, String> nested = new HashMap<>();
        nested.put("propagation", "NESTED");
        nested.put("transactionExists", "Create savepoint");
        nested.put("noTransaction", "Create new");
        nested.put("newTransaction", "Nested (savepoint)");
        nested.put("useCase", "Partial rollback scenarios");
        
        Map<String, String> supports = new HashMap<>();
        supports.put("propagation", "SUPPORTS");
        supports.put("transactionExists", "Join it");
        supports.put("noTransaction", "Execute non-transactionally");
        supports.put("newTransaction", "No");
        supports.put("useCase", "Flexible read operations");
        
        Map<String, String> notSupported = new HashMap<>();
        notSupported.put("propagation", "NOT_SUPPORTED");
        notSupported.put("transactionExists", "Suspend it");
        notSupported.put("noTransaction", "Execute non-transactionally");
        notSupported.put("newTransaction", "Never");
        notSupported.put("useCase", "Read-only, reporting operations");
        
        Map<String, String> mandatory = new HashMap<>();
        mandatory.put("propagation", "MANDATORY");
        mandatory.put("transactionExists", "Join it");
        mandatory.put("noTransaction", "Throw exception");
        mandatory.put("newTransaction", "No");
        mandatory.put("useCase", "Enforce transactional context");
        
        Map<String, String> never = new HashMap<>();
        never.put("propagation", "NEVER");
        never.put("transactionExists", "Throw exception");
        never.put("noTransaction", "Execute non-transactionally");
        never.put("newTransaction", "Never");
        never.put("useCase", "Enforce non-transactional execution");
        
        comparisonTable.put("REQUIRED", required);
        comparisonTable.put("REQUIRES_NEW", requiresNew);
        comparisonTable.put("NESTED", nested);
        comparisonTable.put("SUPPORTS", supports);
        comparisonTable.put("NOT_SUPPORTED", notSupported);
        comparisonTable.put("MANDATORY", mandatory);
        comparisonTable.put("NEVER", never);
        
        response.setResults(comparisonTable);
        
        return response;
    }
}
