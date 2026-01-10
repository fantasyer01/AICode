package com.transaction.training.service;

import com.transaction.training.dto.DemoResponse;
import com.transaction.training.entity.Account;
import com.transaction.training.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RollbackDemoService {
    
    private final AccountRepository accountRepository;
    
    /**
     * Demo: Default rollback behavior (unchecked exceptions)
     */
    @Transactional
    public DemoResponse demonstrateDefaultRollback(boolean throwException) {
        DemoResponse response = DemoResponse.success("DEFAULT_ROLLBACK",
            "By default, @Transactional rolls back on RuntimeException and Error, but commits on checked exceptions.");
        
        try {
            Account account = accountRepository.findById(1L).orElseThrow();
            BigDecimal oldBalance = account.getBalance();
            account.setBalance(account.getBalance().add(new BigDecimal("100")));
            accountRepository.save(account);
            
            if (throwException) {
                throw new RuntimeException("Simulated unchecked exception - transaction will rollback");
            }
            
            Map<String, Object> results = new HashMap<>();
            results.put("oldBalance", oldBalance);
            results.put("newBalance", account.getBalance());
            results.put("committed", true);
            response.setResults(results);
            
        } catch (RuntimeException e) {
            log.warn("Transaction rolled back due to: {}", e.getMessage());
            response.setSuccess(false);
            response.setExplanation("Transaction rolled back on RuntimeException: " + e.getMessage());
        }
        
        response.setCodeSnippet("""
            @Transactional
            public void updateAccount() {
                account.setBalance(newBalance);
                accountRepository.save(account);
                
                // This will cause rollback
                throw new RuntimeException("Error");
            }
            
            // Result: Transaction is rolled back, balance unchanged
            """);
        
        return response;
    }
    
    /**
     * Demo: Checked exception does NOT rollback by default
     */
    @Transactional
    public DemoResponse demonstrateCheckedExceptionNoRollback() {
        DemoResponse response = DemoResponse.success("CHECKED_EXCEPTION_NO_ROLLBACK",
            "Checked exceptions do NOT cause rollback by default. Transaction commits even if checked exception is thrown.");
        
        try {
            Account account = accountRepository.findById(1L).orElseThrow();
            BigDecimal oldBalance = account.getBalance();
            account.setBalance(account.getBalance().add(new BigDecimal("50")));
            accountRepository.save(account);
            
            // Simulate checked exception
            processWithCheckedException();
            
        } catch (IOException e) {
            log.info("Caught checked exception, but transaction will still commit!");
            response.setExplanation("Checked exception thrown, but transaction committed anyway!");
        }
        
        response.setCodeSnippet("""
            @Transactional
            public void updateAccount() throws IOException {
                account.setBalance(newBalance);
                accountRepository.save(account);
                
                throw new IOException("Checked exception");
                // Transaction COMMITS despite exception!
            }
            """);
        
        return response;
    }
    
    private void processWithCheckedException() throws IOException {
        throw new IOException("Simulated checked exception");
    }
    
    /**
     * Demo: Custom rollback with rollbackFor
     */
    @Transactional(rollbackFor = Exception.class)
    public DemoResponse demonstrateRollbackFor() {
        DemoResponse response = DemoResponse.success("ROLLBACK_FOR",
            "rollbackFor attribute specifies additional exceptions that should trigger rollback, including checked exceptions.");
        
        try {
            Account account = accountRepository.findById(1L).orElseThrow();
            account.setBalance(account.getBalance().add(new BigDecimal("75")));
            accountRepository.save(account);
            
            throw new IOException("Checked exception with rollbackFor");
            
        } catch (IOException e) {
            log.info("Transaction rolled back due to rollbackFor configuration");
            response.setSuccess(false);
            response.setExplanation("Transaction rolled back on checked exception due to rollbackFor=Exception.class");
        }
        
        response.setCodeSnippet("""
            @Transactional(rollbackFor = Exception.class)
            public void updateAccount() throws IOException {
                account.setBalance(newBalance);
                accountRepository.save(account);
                
                throw new IOException("Checked exception");
                // With rollbackFor, transaction ROLLS BACK
            }
            """);
        
        return response;
    }
    
    /**
     * Demo: Prevent rollback with noRollbackFor
     */
    @Transactional(noRollbackFor = IllegalArgumentException.class)
    public DemoResponse demonstrateNoRollbackFor(boolean throwException) {
        DemoResponse response = DemoResponse.success("NO_ROLLBACK_FOR",
            "noRollbackFor attribute prevents rollback for specified exceptions, allowing transaction to commit.");
        
        try {
            Account account = accountRepository.findById(1L).orElseThrow();
            BigDecimal oldBalance = account.getBalance();
            account.setBalance(account.getBalance().add(new BigDecimal("25")));
            accountRepository.save(account);
            
            if (throwException) {
                throw new IllegalArgumentException("Exception ignored for rollback");
            }
            
            Map<String, Object> results = new HashMap<>();
            results.put("oldBalance", oldBalance);
            results.put("committed", true);
            response.setResults(results);
            
        } catch (IllegalArgumentException e) {
            log.info("Exception thrown but transaction commits due to noRollbackFor");
            response.setExplanation("Transaction committed despite exception due to noRollbackFor");
        }
        
        response.setCodeSnippet("""
            @Transactional(noRollbackFor = IllegalArgumentException.class)
            public void updateAccount() {
                account.setBalance(newBalance);
                accountRepository.save(account);
                
                throw new IllegalArgumentException("This won't rollback");
                // Transaction COMMITS despite exception
            }
            """);
        
        return response;
    }
    
    /**
     * Demo: Common pitfall - Self-invocation
     */
    public DemoResponse demonstrateSelfInvocation() {
        DemoResponse response = DemoResponse.success("SELF_INVOCATION_PITFALL",
            "Self-invocation bypasses Spring proxy, causing @Transactional to be ignored. Transaction management fails.");
        
        response.setLogs(List.of(
            "Problem: Calling @Transactional method from within same class",
            "Spring AOP proxy is bypassed in self-invocation",
            "Transaction annotations are not processed",
            "Solution: Inject self via ApplicationContext or use separate service"
        ));
        
        response.setCodeSnippet("""
            @Service
            public class MyService {
                // This WON'T work - self-invocation!
                public void publicMethod() {
                    this.transactionalMethod(); // Proxy bypassed!
                }
                
                @Transactional
                private void transactionalMethod() {
                    // Transaction NOT applied!
                }
                
                // Solution 1: Inject self
                @Autowired
                private MyService self;
                
                public void publicMethodFixed() {
                    self.transactionalMethod(); // Works!
                }
                
                // Solution 2: Separate service
                @Autowired
                private OtherService otherService;
                
                public void publicMethodFixed2() {
                    otherService.transactionalMethod(); // Works!
                }
            }
            """);
        
        return response;
    }
    
    /**
     * Demo: Transaction boundary issues
     */
    public DemoResponse demonstrateTransactionBoundary() {
        DemoResponse response = DemoResponse.success("TRANSACTION_BOUNDARY",
            "Understanding where transactions start and end is crucial. Operations outside @Transactional are not protected.");
        
        response.setCodeSnippet("""
            @Service
            public class OrderService {
                @Transactional
                public void processOrder(Order order) {
                    orderRepository.save(order);
                    // Within transaction boundary
                }
                
                public void processOrderIncorrect(Order order) {
                    // NOT in transaction!
                    orderRepository.save(order);
                    
                    updateInventory(); // Separate transaction
                    
                    // If updateInventory fails, order is already saved!
                }
                
                @Transactional
                public void processOrderCorrect(Order order) {
                    // Both in SAME transaction
                    orderRepository.save(order);
                    updateInventory();
                    // If updateInventory fails, both rollback together
                }
                
                @Transactional
                private void updateInventory() {
                    // Transaction logic
                }
            }
            """);
        
        return response;
    }
}
