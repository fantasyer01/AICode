package com.transaction.training.service;

import com.transaction.training.dto.DemoResponse;
import com.transaction.training.entity.Account;
import com.transaction.training.entity.TransactionLog;
import com.transaction.training.repository.AccountRepository;
import com.transaction.training.repository.TransactionLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProgrammaticTransactionService {
    
    private final AccountRepository accountRepository;
    private final TransactionLogRepository transactionLogRepository;
    private final PlatformTransactionManager transactionManager;
    private final TransactionTemplate transactionTemplate;
    
    /**
     * Demonstrate declarative transaction with @Transactional
     */
    @Transactional
    public DemoResponse demonstrateDeclarative() {
        DemoResponse response = DemoResponse.success("DECLARATIVE_TRANSACTION",
            "Declarative transactions use @Transactional annotation. Spring handles transaction lifecycle automatically.");
        
        try {
            Account account = accountRepository.findById(1L).orElseThrow();
            BigDecimal oldBalance = account.getBalance();
            account.setBalance(account.getBalance().add(new BigDecimal("100")));
            accountRepository.save(account);
            
            TransactionLog log = new TransactionLog();
            log.setTransactionType("DECLARATIVE");
            log.setOperation("Update account balance");
            log.setStatus("SUCCESS");
            transactionLogRepository.save(log);
            
            Map<String, Object> results = new HashMap<>();
            results.put("oldBalance", oldBalance);
            results.put("newBalance", account.getBalance());
            response.setResults(results);
            
            response.setCodeSnippet("""
                @Transactional
                public void updateAccount() {
                    Account account = accountRepository.findById(1L).get();
                    account.setBalance(account.getBalance().add(new BigDecimal("100")));
                    accountRepository.save(account);
                    // Transaction commits automatically if no exception
                    // Rolls back automatically on unchecked exceptions
                }
                """);
            
        } catch (Exception e) {
            log.error("Declarative transaction error", e);
            response.setSuccess(false);
        }
        
        return response;
    }
    
    /**
     * Demonstrate TransactionTemplate programmatic transaction
     */
    public DemoResponse demonstrateTransactionTemplate() {
        DemoResponse response = DemoResponse.success("TRANSACTION_TEMPLATE",
            "TransactionTemplate provides programmatic transaction control with callback mechanism.");
        
        try {
            BigDecimal result = transactionTemplate.execute(status -> {
                Account account = accountRepository.findById(1L).orElseThrow();
                BigDecimal oldBalance = account.getBalance();
                account.setBalance(account.getBalance().add(new BigDecimal("50")));
                accountRepository.save(account);
                
                TransactionLog log = new TransactionLog();
                log.setTransactionType("TRANSACTION_TEMPLATE");
                log.setOperation("Update via TransactionTemplate");
                log.setStatus("SUCCESS");
                transactionLogRepository.save(log);
                
                return account.getBalance();
            });
            
            Map<String, Object> results = new HashMap<>();
            results.put("newBalance", result);
            response.setResults(results);
            
            response.setCodeSnippet("""
                @Autowired
                private TransactionTemplate transactionTemplate;
                
                public void updateAccount() {
                    transactionTemplate.execute(status -> {
                        Account account = accountRepository.findById(1L).get();
                        account.setBalance(account.getBalance().add(new BigDecimal("50")));
                        accountRepository.save(account);
                        
                        // Explicit rollback if needed
                        // status.setRollbackOnly();
                        
                        return account.getBalance();
                    });
                }
                """);
            
        } catch (Exception e) {
            log.error("TransactionTemplate error", e);
            response.setSuccess(false);
        }
        
        return response;
    }
    
    /**
     * Demonstrate PlatformTransactionManager direct usage
     */
    public DemoResponse demonstratePlatformTransactionManager() {
        DemoResponse response = DemoResponse.success("PLATFORM_TRANSACTION_MANAGER",
            "PlatformTransactionManager provides lowest-level programmatic transaction control with explicit commit/rollback.");
        
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("ManualTransaction");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        
        TransactionStatus status = transactionManager.getTransaction(def);
        
        try {
            Account account = accountRepository.findById(1L).orElseThrow();
            BigDecimal oldBalance = account.getBalance();
            account.setBalance(account.getBalance().add(new BigDecimal("75")));
            accountRepository.save(account);
            
            TransactionLog log = new TransactionLog();
            log.setTransactionType("PLATFORM_TX_MANAGER");
            log.setOperation("Update via PlatformTransactionManager");
            log.setStatus("SUCCESS");
            transactionLogRepository.save(log);
            
            // Explicit commit
            transactionManager.commit(status);
            
            Map<String, Object> results = new HashMap<>();
            results.put("oldBalance", oldBalance);
            results.put("newBalance", account.getBalance());
            response.setResults(results);
            
            response.setCodeSnippet("""
                @Autowired
                private PlatformTransactionManager transactionManager;
                
                public void updateAccount() {
                    DefaultTransactionDefinition def = new DefaultTransactionDefinition();
                    def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
                    
                    TransactionStatus status = transactionManager.getTransaction(def);
                    
                    try {
                        Account account = accountRepository.findById(1L).get();
                        account.setBalance(account.getBalance().add(new BigDecimal("75")));
                        accountRepository.save(account);
                        
                        transactionManager.commit(status);
                    } catch (Exception e) {
                        transactionManager.rollback(status);
                        throw e;
                    }
                }
                """);
            
        } catch (Exception e) {
            log.error("PlatformTransactionManager error", e);
            transactionManager.rollback(status);
            response.setSuccess(false);
        }
        
        return response;
    }
}
