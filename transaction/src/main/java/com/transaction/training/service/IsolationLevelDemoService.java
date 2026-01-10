package com.transaction.training.service;

import com.transaction.training.dto.DemoResponse;
import com.transaction.training.entity.Account;
import com.transaction.training.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class IsolationLevelDemoService {
    
    private final AccountRepository accountRepository;
    
    /**
     * Demo: READ_UNCOMMITTED isolation level
     * Demonstrates dirty read - reading uncommitted changes from another transaction
     */
    public DemoResponse demonstrateReadUncommitted() {
        DemoResponse response = DemoResponse.success("ISOLATION_READ_UNCOMMITTED",
            "READ_UNCOMMITTED allows dirty reads - transaction can read uncommitted changes from other transactions.");
        
        List<DemoResponse.DemoStep> steps = new ArrayList<>();
        List<String> logs = new ArrayList<>();
        Map<String, Object> results = new HashMap<>();
        
        try {
            // Get initial balance
            Account initialAccount = accountRepository.findById(1L).orElseThrow();
            BigDecimal initialBalance = initialAccount.getBalance();
            
            steps.add(DemoResponse.DemoStep.builder()
                    .stepNumber(1)
                    .description("Transaction T1 starts with READ_UNCOMMITTED isolation")
                    .status("STARTED")
                    .details("Initial balance: " + initialBalance)
                    .build());
            logs.add("T1: BEGIN TRANSACTION with READ_UNCOMMITTED");
            logs.add("T1: SELECT balance FROM accounts WHERE id = 1 -- Result: " + initialBalance);
            
            // Start T1 that will read twice
            CompletableFuture<Map<String, BigDecimal>> t1Future = CompletableFuture.supplyAsync(() -> 
                readUncommittedTransaction1()
            );
            
            Thread.sleep(100); // Allow T1 to start and do first read
            
            steps.add(DemoResponse.DemoStep.builder()
                    .stepNumber(2)
                    .description("Transaction T2 modifies balance but doesn't commit")
                    .status("IN_PROGRESS")
                    .build());
            
            // T2 modifies but will rollback
            CompletableFuture<BigDecimal> t2Future = CompletableFuture.supplyAsync(() -> 
                modifyWithoutCommit()
            );
            
            Thread.sleep(200); // T1 reads dirty data from T2
            
            Map<String, BigDecimal> t1Reads = t1Future.get(5, TimeUnit.SECONDS);
            BigDecimal dirtyReadValue = t1Reads.get("dirtyRead");
            
            logs.add("T2: BEGIN TRANSACTION");
            logs.add("T2: UPDATE accounts SET balance = " + dirtyReadValue + " WHERE id = 1 (NOT COMMITTED)");
            logs.add("T1: SELECT balance FROM accounts WHERE id = 1 -- Result: " + dirtyReadValue + " (DIRTY READ!)");
            
            // Wait for T2 to rollback
            try {
                t2Future.get(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                // Expected - T2 throws exception to rollback
            }
            
            logs.add("T2: ROLLBACK -- Changes are discarded");
            logs.add("T1: COMMIT with potentially invalid data (balance we read was rolled back!)");
            
            steps.add(DemoResponse.DemoStep.builder()
                    .stepNumber(3)
                    .description("Transaction T1 read uncommitted (dirty) data from T2")
                    .status("DIRTY_READ")
                    .details(String.format("T1 read dirty value: %s, but T2 rolled back!", dirtyReadValue))
                    .build());
            
            results.put("issue", "Dirty Read Problem");
            results.put("initialBalance", initialBalance);
            results.put("dirtyReadValue", dirtyReadValue);
            results.put("t2RolledBack", true);
            results.put("problem", "T1 read data that was never committed - data integrity violation");
            
        } catch (Exception e) {
            log.error("Error in READ_UNCOMMITTED demo", e);
            response.setSuccess(false);
            logs.add("ERROR: " + e.getMessage());
        }
        
        response.setSteps(steps);
        response.setLogs(logs);
        response.setResults(results);
        response.setCodeSnippet("""
            @Transactional(isolation = Isolation.READ_UNCOMMITTED)
            public BigDecimal readAccountTwice(Long id) {
                // First read
                Account account1 = accountRepository.findById(id).get();
                BigDecimal balance1 = account1.getBalance();
                
                // Another transaction modifies but doesn't commit
                Thread.sleep(200);
                
                // Second read - may see uncommitted changes!
                Account account2 = accountRepository.findById(id).get();
                BigDecimal balance2 = account2.getBalance(); // Dirty read!
                
                // Problem: If other transaction rolls back, we've read invalid data
                return balance2;
            }
            """);
        
        return response;
    }
    
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public Map<String, BigDecimal> readUncommittedTransaction1() {
        try {
            // First read
            Account account1 = accountRepository.findById(1L).orElseThrow();
            BigDecimal firstRead = account1.getBalance();
            log.info("T1 READ_UNCOMMITTED: First read balance = {}", firstRead);
            
            Thread.sleep(200); // Wait for T2 to modify (but not commit)
            
            // Second read - will see uncommitted changes from T2
            Account account2 = accountRepository.findById(1L).orElseThrow();
            BigDecimal dirtyRead = account2.getBalance();
            log.info("T1 READ_UNCOMMITTED: Second read (dirty) balance = {} (T2 not committed yet!)", dirtyRead);
            
            Thread.sleep(100); // Allow T2 to rollback
            
            Map<String, BigDecimal> reads = new HashMap<>();
            reads.put("firstRead", firstRead);
            reads.put("dirtyRead", dirtyRead);
            return reads;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Transactional
    public BigDecimal modifyWithoutCommit() {
        try {
            Thread.sleep(50); // Small delay
            Account account = accountRepository.findById(1L).orElseThrow();
            BigDecimal newBalance = account.getBalance().add(new BigDecimal("500"));
            account.setBalance(newBalance);
            accountRepository.save(account);
            log.info("T2: Modified balance to {} but NOT committing (will rollback)", newBalance);
            
            Thread.sleep(300); // Hold the modified data for T1 to read
            
            // Simulate rollback by throwing exception
            throw new RuntimeException("Simulating T2 rollback - changes discarded");
        } catch (Exception e) {
            log.warn("T2: Rolling back changes - {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Demo: READ_COMMITTED isolation level
     * Demonstrates non-repeatable read - reading different values in same transaction
     */
    public DemoResponse demonstrateReadCommitted() {
        DemoResponse response = DemoResponse.success("ISOLATION_READ_COMMITTED",
            "READ_COMMITTED prevents dirty reads but allows non-repeatable reads - same query may return different results.");
        
        List<DemoResponse.DemoStep> steps = new ArrayList<>();
        List<String> logs = new ArrayList<>();
        Map<String, Object> results = new HashMap<>();
        
        try {
            // Get initial balance
            Account initialAccount = accountRepository.findById(1L).orElseThrow();
            BigDecimal initialBalance = initialAccount.getBalance();
            
            steps.add(DemoResponse.DemoStep.builder()
                    .stepNumber(1)
                    .description("Transaction T1 starts and reads initial balance")
                    .status("STARTED")
                    .details("Initial balance: " + initialBalance)
                    .build());
            logs.add("T1: BEGIN TRANSACTION with READ_COMMITTED");
            logs.add("T1: SELECT balance FROM accounts WHERE id = 1 -- Result: " + initialBalance);
            
            // Start T1 that will read twice
            CompletableFuture<Map<String, BigDecimal>> t1Future = CompletableFuture.supplyAsync(() -> 
                readCommittedTransaction1()
            );
            
            Thread.sleep(100); // Allow T1 to do first read
            
            steps.add(DemoResponse.DemoStep.builder()
                    .stepNumber(2)
                    .description("Transaction T2 modifies and commits the balance")
                    .status("IN_PROGRESS")
                    .build());
            
            // T2 modifies and commits
            CompletableFuture<BigDecimal> t2Future = CompletableFuture.supplyAsync(() -> 
                modifyAndCommit()
            );
            
            BigDecimal newBalance = t2Future.get(5, TimeUnit.SECONDS);
            logs.add("T2: BEGIN TRANSACTION");
            logs.add("T2: UPDATE accounts SET balance = " + newBalance + " WHERE id = 1");
            logs.add("T2: COMMIT -- Changes are now visible to READ_COMMITTED");
            
            Thread.sleep(100); // Allow T1 to do second read
            
            Map<String, BigDecimal> t1Reads = t1Future.get(5, TimeUnit.SECONDS);
            
            steps.add(DemoResponse.DemoStep.builder()
                    .stepNumber(3)
                    .description("Transaction T1 reads again in same transaction")
                    .status("NON_REPEATABLE_READ")
                    .details(String.format("First read: %s, Second read: %s", t1Reads.get("firstRead"), t1Reads.get("secondRead")))
                    .build());
            
            logs.add("T1: SELECT balance FROM accounts WHERE id = 1 -- Result: " + t1Reads.get("secondRead") + " (CHANGED!)");
            logs.add("T1: COMMIT -- Non-repeatable read occurred!");
            
            results.put("issue", "Non-Repeatable Read Problem");
            results.put("firstRead", t1Reads.get("firstRead"));
            results.put("secondRead", t1Reads.get("secondRead"));
            results.put("valueChanged", !t1Reads.get("firstRead").equals(t1Reads.get("secondRead")));
            
        } catch (Exception e) {
            log.error("Error in READ_COMMITTED demo", e);
            response.setSuccess(false);
            logs.add("ERROR: " + e.getMessage());
        }
        
        response.setSteps(steps);
        response.setLogs(logs);
        response.setResults(results);
        response.setCodeSnippet("""
            @Transactional(isolation = Isolation.READ_COMMITTED)
            public void demonstrateNonRepeatableRead() {
                // First read
                Account account1 = accountRepository.findById(1L).get();
                BigDecimal balance1 = account1.getBalance(); // Returns 1000
                
                // Another transaction commits changes here
                Thread.sleep(200);
                
                // Second read in same transaction
                Account account2 = accountRepository.findById(1L).get();
                BigDecimal balance2 = account2.getBalance(); // Returns 1500
                
                // balance1 != balance2 - Non-repeatable Read Problem!
            }
            """);
        
        return response;
    }
    
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Map<String, BigDecimal> readCommittedTransaction1() {
        try {
            // First read
            Account account1 = accountRepository.findById(1L).orElseThrow();
            BigDecimal firstRead = account1.getBalance();
            log.info("T1 READ_COMMITTED: First read balance = {}", firstRead);
            
            Thread.sleep(300); // Wait for T2 to commit
            
            // Second read in same transaction
            Account account2 = accountRepository.findById(1L).orElseThrow();
            BigDecimal secondRead = account2.getBalance();
            log.info("T1 READ_COMMITTED: Second read balance = {} (changed: {})", secondRead, !firstRead.equals(secondRead));
            
            Map<String, BigDecimal> reads = new HashMap<>();
            reads.put("firstRead", firstRead);
            reads.put("secondRead", secondRead);
            return reads;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Transactional
    public BigDecimal modifyAndCommit() {
        try {
            Thread.sleep(50); // Small delay
            Account account = accountRepository.findById(1L).orElseThrow();
            BigDecimal newBalance = account.getBalance().add(new BigDecimal("500"));
            account.setBalance(newBalance);
            accountRepository.save(account);
            log.info("T2: Modified balance to {} and committing", newBalance);
            return newBalance;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Demo: REPEATABLE_READ isolation level
     * Demonstrates phantom read - new rows appearing in subsequent queries
     */
    public DemoResponse demonstrateRepeatableRead() {
        DemoResponse response = DemoResponse.success("ISOLATION_REPEATABLE_READ",
            "REPEATABLE_READ prevents dirty and non-repeatable reads but allows phantom reads - new rows may appear.");
        
        List<DemoResponse.DemoStep> steps = new ArrayList<>();
        List<String> logs = new ArrayList<>();
        Map<String, Object> results = new HashMap<>();
        
        try {
            // Get initial count
            long initialCount = accountRepository.count();
            
            steps.add(DemoResponse.DemoStep.builder()
                    .stepNumber(1)
                    .description("Transaction T1 starts and counts records")
                    .status("STARTED")
                    .details("Initial count: " + initialCount)
                    .build());
            logs.add("T1: BEGIN TRANSACTION with REPEATABLE_READ");
            logs.add("T1: SELECT COUNT(*) FROM accounts -- Result: " + initialCount);
            
            // Start T1 that will count twice
            CompletableFuture<Map<String, Long>> t1Future = CompletableFuture.supplyAsync(() -> 
                repeatableReadTransaction1()
            );
            
            Thread.sleep(100); // Allow T1 to do first count
            
            steps.add(DemoResponse.DemoStep.builder()
                    .stepNumber(2)
                    .description("Transaction T2 inserts new account and commits")
                    .status("IN_PROGRESS")
                    .build());
            
            // T2 inserts new record and commits
            CompletableFuture<Long> t2Future = CompletableFuture.supplyAsync(() -> 
                insertNewAccount()
            );
            
            Long newAccountId = t2Future.get(5, TimeUnit.SECONDS);
            logs.add("T2: BEGIN TRANSACTION");
            logs.add("T2: INSERT INTO accounts (id, user_name, balance) VALUES (" + newAccountId + ", 'phantom', 1000)");
            logs.add("T2: COMMIT -- New row is now visible (phantom read potential)");
            
            Thread.sleep(100); // Allow T1 to do second count
            
            Map<String, Long> t1Counts = t1Future.get(5, TimeUnit.SECONDS);
            
            boolean phantomOccurred = !t1Counts.get("firstCount").equals(t1Counts.get("secondCount"));
            
            steps.add(DemoResponse.DemoStep.builder()
                    .stepNumber(3)
                    .description("Transaction T1 counts again in same transaction")
                    .status(phantomOccurred ? "PHANTOM_READ" : "NO_PHANTOM")
                    .details(String.format("First count: %d, Second count: %d", t1Counts.get("firstCount"), t1Counts.get("secondCount")))
                    .build());
            
            logs.add("T1: SELECT COUNT(*) FROM accounts -- Result: " + t1Counts.get("secondCount"));
            if (phantomOccurred) {
                logs.add("T1: PHANTOM READ occurred! Count changed in same transaction.");
            } else {
                logs.add("T1: No phantom read (MySQL InnoDB uses gap locks to prevent this)");
            }
            logs.add("T1: COMMIT");
            
            results.put("issue", phantomOccurred ? "Phantom Read Occurred" : "No Phantom Read (MySQL InnoDB Protection)");
            results.put("firstCount", t1Counts.get("firstCount"));
            results.put("secondCount", t1Counts.get("secondCount"));
            results.put("phantomOccurred", phantomOccurred);
            results.put("note", "MySQL InnoDB uses gap locks to prevent phantom reads in REPEATABLE_READ");
            
        } catch (Exception e) {
            log.error("Error in REPEATABLE_READ demo", e);
            response.setSuccess(false);
            logs.add("ERROR: " + e.getMessage());
        }
        
        response.setSteps(steps);
        response.setLogs(logs);
        response.setResults(results);
        response.setCodeSnippet("""
            @Transactional(isolation = Isolation.REPEATABLE_READ)
            public void demonstratePhantomRead() {
                // First query
                List<Account> accounts1 = accountRepository.findAll();
                int count1 = accounts1.size(); // Returns 5 accounts
                
                // Another transaction inserts new row and commits
                Thread.sleep(200);
                
                // Second query in same transaction
                List<Account> accounts2 = accountRepository.findAll();
                int count2 = accounts2.size(); // May return 6 accounts
                
                // count1 != count2 - Phantom Read Problem!
                // Note: MySQL InnoDB prevents this with gap locks
            }
            """);
        
        return response;
    }
    
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Map<String, Long> repeatableReadTransaction1() {
        try {
            // First count
            long firstCount = accountRepository.count();
            log.info("T1 REPEATABLE_READ: First count = {}", firstCount);
            
            Thread.sleep(300); // Wait for T2 to commit
            
            // Second count in same transaction
            long secondCount = accountRepository.count();
            log.info("T1 REPEATABLE_READ: Second count = {} (changed: {})", secondCount, firstCount != secondCount);
            
            Map<String, Long> counts = new HashMap<>();
            counts.put("firstCount", firstCount);
            counts.put("secondCount", secondCount);
            return counts;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Transactional
    public Long insertNewAccount() {
        try {
            Thread.sleep(50); // Small delay
            Account newAccount = new Account();
            newAccount.setUserName("phantom_user");
            newAccount.setBalance(new BigDecimal("1000"));
            Account saved = accountRepository.save(newAccount);
            log.info("T2: Inserted new account with id = {} and committing", saved.getAccountId());
            return saved.getAccountId();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Demo: SERIALIZABLE isolation level
     * Demonstrates full isolation - prevents dirty, non-repeatable, and phantom reads
     */
    public DemoResponse demonstrateSerializable() {
        DemoResponse response = DemoResponse.success("ISOLATION_SERIALIZABLE",
            "SERIALIZABLE provides complete isolation. Transactions execute as if they were serial - highest isolation, lowest concurrency.");
        
        List<DemoResponse.DemoStep> steps = new ArrayList<>();
        List<String> logs = new ArrayList<>();
        Map<String, Object> results = new HashMap<>();
        
        try {
            // Get initial balance
            Account initialAccount = accountRepository.findById(1L).orElseThrow();
            BigDecimal initialBalance = initialAccount.getBalance();
            
            steps.add(DemoResponse.DemoStep.builder()
                    .stepNumber(1)
                    .description("Transaction T1 starts with SERIALIZABLE isolation")
                    .status("STARTED")
                    .details("Initial balance: " + initialBalance)
                    .build());
            logs.add("T1: BEGIN TRANSACTION with SERIALIZABLE");
            logs.add("T1: SELECT balance FROM accounts WHERE id = 1 FOR SHARE -- Acquires shared lock");
            logs.add("T1: Result: " + initialBalance);
            
            // Start T1 with SERIALIZABLE
            CompletableFuture<BigDecimal> t1Future = CompletableFuture.supplyAsync(() -> 
                serializableTransaction1()
            );
            
            Thread.sleep(100); // Allow T1 to acquire locks
            
            steps.add(DemoResponse.DemoStep.builder()
                    .stepNumber(2)
                    .description("Transaction T2 tries to access same data")
                    .status("WAITING")
                    .details("T2 must wait for T1 to complete due to SERIALIZABLE locks")
                    .build());
            logs.add("T2: BEGIN TRANSACTION with SERIALIZABLE");
            logs.add("T2: Attempting to SELECT balance FROM accounts WHERE id = 1");
            logs.add("T2: WAITING... (blocked by T1's locks)");
            
            // Start T2 - it will be blocked or wait
            CompletableFuture<String> t2Future = CompletableFuture.supplyAsync(() -> 
                serializableTransaction2()
            );
            
            Thread.sleep(200); // T2 is waiting
            
            BigDecimal t1Result = t1Future.get(5, TimeUnit.SECONDS);
            logs.add("T1: UPDATE accounts SET balance = " + t1Result + " WHERE id = 1");
            logs.add("T1: COMMIT -- Releasing all locks");
            
            steps.add(DemoResponse.DemoStep.builder()
                    .stepNumber(3)
                    .description("Transaction T1 completes successfully")
                    .status("SUCCESS")
                    .details("Updated balance to: " + t1Result)
                    .build());
            
            String t2Result = t2Future.get(5, TimeUnit.SECONDS);
            logs.add("T2: Now can proceed after T1 released locks");
            logs.add("T2: " + t2Result);
            logs.add("T2: COMMIT");
            
            steps.add(DemoResponse.DemoStep.builder()
                    .stepNumber(4)
                    .description("Transaction T2 executes after T1")
                    .status("SUCCESS")
                    .details("Transactions executed serially - no concurrency issues")
                    .build());
            
            results.put("isolation", "SERIALIZABLE");
            results.put("dirtyRead", "PREVENTED");
            results.put("nonRepeatableRead", "PREVENTED");
            results.put("phantomRead", "PREVENTED");
            results.put("tradeoff", "High safety, low concurrency, potential deadlocks");
            results.put("t1_final_balance", t1Result);
            
        } catch (Exception e) {
            log.error("Error in SERIALIZABLE demo", e);
            response.setSuccess(false);
            logs.add("ERROR: " + e.getMessage());
        }
        
        response.setSteps(steps);
        response.setLogs(logs);
        response.setResults(results);
        response.setCodeSnippet("""
            @Transactional(isolation = Isolation.SERIALIZABLE)
            public void safeTransaction() {
                // Complete isolation from other transactions
                Account account = accountRepository.findById(1L).get();
                BigDecimal balance1 = account.getBalance();
                
                // Other transactions must wait...
                Thread.sleep(200);
                
                account.setBalance(balance1.add(new BigDecimal("100")));
                accountRepository.save(account);
                
                // No dirty, non-repeatable, or phantom reads possible
                // But may cause lock wait timeouts or deadlocks
            }
            """);
        
        return response;
    }
    
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public BigDecimal serializableTransaction1() {
        try {
            Account account = accountRepository.findById(1L).orElseThrow();
            BigDecimal currentBalance = account.getBalance();
            log.info("T1 SERIALIZABLE: Read balance = {} (locks acquired)", currentBalance);
            
            Thread.sleep(300); // Hold locks for a while
            
            BigDecimal newBalance = currentBalance.add(new BigDecimal("100"));
            account.setBalance(newBalance);
            accountRepository.save(account);
            log.info("T1 SERIALIZABLE: Updated balance to {} and committing", newBalance);
            
            return newBalance;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public String serializableTransaction2() {
        try {
            Thread.sleep(50); // Start slightly after T1
            log.info("T2 SERIALIZABLE: Attempting to read account (may wait for T1)");
            Thread.sleep(400); // Wait for T1 to complete
            Account account = accountRepository.findById(1L).orElseThrow();
            log.info("T2 SERIALIZABLE: Successfully read balance = {} after T1 completed", account.getBalance());
            return "Successfully read after T1 completed: " + account.getBalance();
        } catch (Exception e) {
            log.warn("T2 SERIALIZABLE: Error or timeout - {}", e.getMessage());
            return "Waited for T1 to complete";
        }
    }
    
    /**
     * Comparison table of all isolation levels
     */
    public DemoResponse compareIsolationLevels() {
        DemoResponse response = DemoResponse.success("ISOLATION_COMPARISON",
            "Comparison of transaction isolation levels and the read phenomena they prevent.");
        
        Map<String, Object> comparisonTable = new HashMap<>();
        
        Map<String, String> readUncommitted = new HashMap<>();
        readUncommitted.put("level", "READ_UNCOMMITTED");
        readUncommitted.put("dirtyRead", "Possible");
        readUncommitted.put("nonRepeatableRead", "Possible");
        readUncommitted.put("phantomRead", "Possible");
        readUncommitted.put("performance", "Highest");
        
        Map<String, String> readCommitted = new HashMap<>();
        readCommitted.put("level", "READ_COMMITTED");
        readCommitted.put("dirtyRead", "Prevented");
        readCommitted.put("nonRepeatableRead", "Possible");
        readCommitted.put("phantomRead", "Possible");
        readCommitted.put("performance", "High");
        
        Map<String, String> repeatableRead = new HashMap<>();
        repeatableRead.put("level", "REPEATABLE_READ");
        repeatableRead.put("dirtyRead", "Prevented");
        repeatableRead.put("nonRepeatableRead", "Prevented");
        repeatableRead.put("phantomRead", "Possible (MySQL prevents)");
        repeatableRead.put("performance", "Medium");
        
        Map<String, String> serializable = new HashMap<>();
        serializable.put("level", "SERIALIZABLE");
        serializable.put("dirtyRead", "Prevented");
        serializable.put("nonRepeatableRead", "Prevented");
        serializable.put("phantomRead", "Prevented");
        serializable.put("performance", "Lowest");
        
        comparisonTable.put("READ_UNCOMMITTED", readUncommitted);
        comparisonTable.put("READ_COMMITTED", readCommitted);
        comparisonTable.put("REPEATABLE_READ", repeatableRead);
        comparisonTable.put("SERIALIZABLE", serializable);
        
        response.setResults(comparisonTable);
        
        return response;
    }
}
