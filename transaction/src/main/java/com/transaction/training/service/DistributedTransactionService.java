package com.transaction.training.service;

import com.transaction.training.dto.DemoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DistributedTransactionService {
    
    /**
     * Demonstrate Seata AT mode (conceptual)
     */
    public DemoResponse demonstrateSeataAT() {
        DemoResponse response = DemoResponse.success("SEATA_AT_MODE",
            "Seata AT (Automatic Transaction) mode provides automatic distributed transaction management with minimal code changes.");
        
        response.setLogs(List.of(
            "Phase 1 - Execution Phase:",
            "1. TM (Transaction Manager) begins global transaction",
            "2. RM (Resource Manager) executes business SQL",
            "3. RM records before/after images to undo_log",
            "4. RM registers branch transaction with TC (Transaction Coordinator)",
            "5. Local transaction commits, releases locks",
            "",
            "Phase 2 - Completion Phase:",
            "Success: TM requests TC to commit → RMs delete undo_logs asynchronously",
            "Failure: TM requests TC to rollback → RMs use undo_logs to compensate"
        ));
        
        response.setCodeSnippet("""
            // Service A - Order Service
            @GlobalTransactional(name = "create-order", rollbackFor = Exception.class)
            public void createOrder(Order order) {
                // 1. Create order locally
                orderService.create(order);
                
                // 2. Call Service B to deduct inventory (via RPC)
                inventoryService.deduct(order.getProductId(), order.getQuantity());
                
                // 3. Call Service C to deduct account balance
                accountService.deduct(order.getUserId(), order.getAmount());
                
                // If any step fails, Seata automatically rolls back all branches
            }
            
            // Service B - Inventory Service  
            public void deduct(Long productId, Integer quantity) {
                // Seata intercepts this and records undo_log
                inventoryRepository.updateQuantity(productId, quantity);
                // Local commit, but global transaction not finished
            }
            
            // Service C - Account Service
            public void deduct(Long userId, BigDecimal amount) {
                accountRepository.updateBalance(userId, amount);
            }
            
            // Configuration
            seata:
              registry:
                type: nacos
              config:
                type: nacos
              tx-service-group: my_tx_group
            """);
        
        response.setExplanation(
            "AT mode is non-intrusive, requiring only @GlobalTransactional annotation. " +
            "Seata automatically handles branch registration, commit, and rollback. " +
            "Best for scenarios where services share same database or use supported databases."
        );
        
        return response;
    }
    
    /**
     * Demonstrate Seata TCC mode (conceptual)
     */
    public DemoResponse demonstrateSeataTCC() {
        DemoResponse response = DemoResponse.success("SEATA_TCC_MODE",
            "Seata TCC (Try-Confirm-Cancel) mode requires implementing three methods for each business operation.");
        
        response.setLogs(List.of(
            "TCC Pattern Phases:",
            "1. Try Phase: Reserve resources, pre-validation",
            "   - Check and reserve inventory",
            "   - Freeze account balance",
            "   - Business logic validation",
            "",
            "2. Confirm Phase: Execute actual business (if all Try succeed)",
            "   - Deduct reserved inventory",
            "   - Deduct frozen balance",
            "   - Complete order creation",
            "",
            "3. Cancel Phase: Release resources (if any Try fails)",
            "   - Release reserved inventory",
            "   - Unfreeze account balance",
            "   - Rollback order state"
        ));
        
        response.setCodeSnippet("""
            @LocalTCC
            public interface AccountTccAction {
                
                @TwoPhaseBusinessAction(name = "prepareAccount", commitMethod = "commit", rollbackMethod = "rollback")
                boolean prepare(@BusinessActionContextParameter(paramName = "userId") Long userId,
                               @BusinessActionContextParameter(paramName = "amount") BigDecimal amount);
                
                boolean commit(BusinessActionContext context);
                
                boolean rollback(BusinessActionContext context);
            }
            
            @Service
            public class AccountTccActionImpl implements AccountTccAction {
                
                @Override
                @Transactional
                public boolean prepare(Long userId, BigDecimal amount) {
                    // Try phase: Freeze balance (don't actually deduct yet)
                    Account account = accountRepository.findById(userId).get();
                    if (account.getBalance().compareTo(amount) < 0) {
                        return false; // Insufficient balance
                    }
                    account.setFrozenAmount(account.getFrozenAmount().add(amount));
                    accountRepository.save(account);
                    return true;
                }
                
                @Override
                @Transactional
                public boolean commit(BusinessActionContext context) {
                    // Confirm phase: Actually deduct the frozen amount
                    Long userId = context.getActionContext("userId", Long.class);
                    BigDecimal amount = context.getActionContext("amount", BigDecimal.class);
                    
                    Account account = accountRepository.findById(userId).get();
                    account.setBalance(account.getBalance().subtract(amount));
                    account.setFrozenAmount(account.getFrozenAmount().subtract(amount));
                    accountRepository.save(account);
                    return true;
                }
                
                @Override
                @Transactional
                public boolean rollback(BusinessActionContext context) {
                    // Cancel phase: Unfreeze the amount
                    Long userId = context.getActionContext("userId", Long.class);
                    BigDecimal amount = context.getActionContext("amount", BigDecimal.class);
                    
                    Account account = accountRepository.findById(userId).get();
                    account.setFrozenAmount(account.getFrozenAmount().subtract(amount));
                    accountRepository.save(account);
                    return true;
                }
            }
            
            // Usage in main service
            @GlobalTransactional
            public void createOrder(Order order) {
                // TCC actions are invoked automatically by Seata
                accountTccAction.prepare(order.getUserId(), order.getAmount());
                inventoryTccAction.prepare(order.getProductId(), order.getQuantity());
                // If all prepare succeed, Seata calls commit on all
                // If any fail, Seata calls rollback on all
            }
            """);
        
        response.setExplanation(
            "TCC provides better isolation and performance than AT mode, but requires more code. " +
            "Suitable for scenarios needing fine-grained control or when database doesn't support AT mode. " +
            "Must handle idempotency and ensure Cancel can succeed even if Try fails."
        );
        
        return response;
    }
    
    /**
     * Demonstrate Seata SAGA mode (conceptual)
     */
    public DemoResponse demonstrateSeataSAGA() {
        DemoResponse response = DemoResponse.success("SEATA_SAGA_MODE",
            "Seata SAGA mode is designed for long-running transactions with compensating transactions for each step.");
        
        response.setLogs(List.of(
            "SAGA Pattern Workflow:",
            "1. Define state machine with forward and compensating actions",
            "2. Execute forward actions sequentially",
            "3. If any step fails, execute compensating actions in reverse order",
            "4. Eventually consistent - no locks held during execution",
            "",
            "Use Cases:",
            "- Long-running business processes (order fulfillment, payment)",
            "- Cross-system transactions with legacy systems",
            "- Scenarios where locks cannot be held long-term"
        ));
        
        response.setCodeSnippet("""
            // SAGA State Machine Definition (JSON)
            {
              "Name": "OrderSaga",
              "Comment": "Order creation saga",
              "StartState": "CreateOrder",
              "Version": "1.0.0",
              "States": {
                "CreateOrder": {
                  "Type": "ServiceTask",
                  "ServiceName": "orderService",
                  "ServiceMethod": "create",
                  "CompensateState": "CancelOrder",
                  "Next": "DeductInventory"
                },
                "DeductInventory": {
                  "Type": "ServiceTask",
                  "ServiceName": "inventoryService",
                  "ServiceMethod": "deduct",
                  "CompensateState": "RestoreInventory",
                  "Next": "DeductBalance"
                },
                "DeductBalance": {
                  "Type": "ServiceTask",
                  "ServiceName": "accountService",
                  "ServiceMethod": "deduct",
                  "CompensateState": "RestoreBalance",
                  "Next": "Succeed"
                },
                "Succeed": {
                  "Type": "Succeed"
                },
                "CancelOrder": {
                  "Type": "ServiceTask",
                  "ServiceName": "orderService",
                  "ServiceMethod": "cancel"
                },
                "RestoreInventory": {
                  "Type": "ServiceTask",
                  "ServiceName": "inventoryService",
                  "ServiceMethod": "restore"
                },
                "RestoreBalance": {
                  "Type": "ServiceTask",
                  "ServiceName": "accountService",
                  "ServiceMethod": "restore"
                }
              }
            }
            
            // Service Implementation
            @Service
            public class OrderService {
                public void create(Order order) {
                    orderRepository.save(order);
                }
                
                public void cancel(Order order) {
                    // Compensating action
                    order.setStatus("CANCELLED");
                    orderRepository.save(order);
                }
            }
            
            // Start SAGA
            String sagaId = sagaEngine.start("OrderSaga", orderContext);
            // SAGA engine handles execution and compensation automatically
            """);
        
        response.setExplanation(
            "SAGA mode is ideal for complex, long-running workflows. " +
            "No distributed locks, better scalability. " +
            "Requires careful design of compensating actions and handling eventual consistency."
        );
        
        return response;
    }
    
    /**
     * Demonstrate 2PC pattern
     */
    public DemoResponse demonstrate2PC() {
        DemoResponse response = DemoResponse.success("TWO_PHASE_COMMIT",
            "Two-Phase Commit (2PC) is the classic distributed transaction protocol used by XA transactions.");
        
        response.setResults(Map.of(
            "phase1", "Prepare Phase - All participants vote to commit or abort",
            "phase2", "Commit Phase - Coordinator tells all to commit or rollback",
            "advantages", List.of("Strong consistency", "ACID guarantees"),
            "disadvantages", List.of("Blocking protocol", "Single point of failure", "High latency")
        ));
        
        response.setCodeSnippet("""
            // 2PC Protocol Flow
            
            // Phase 1: Prepare (Voting)
            Coordinator: PREPARE transaction T1
            
            Participant A:
              - Execute transaction operations
              - Write undo and redo logs
              - Acquire locks
              - Vote: YES (prepared) or NO (abort)
            
            Participant B:
              - Execute transaction operations
              - Write undo and redo logs
              - Acquire locks
              - Vote: YES (prepared) or NO (abort)
            
            // Phase 2: Commit or Abort
            If ALL vote YES:
              Coordinator: COMMIT T1
              Participant A: Commit and release locks
              Participant B: Commit and release locks
            
            If ANY vote NO:
              Coordinator: ABORT T1
              Participant A: Rollback and release locks
              Participant B: Rollback and release locks
            
            // Problem Scenarios:
            1. Coordinator fails after PREPARE: Participants block indefinitely
            2. Participant fails after voting: Others wait
            3. Network partition: Uncertainty state
            
            // XA Implementation in Java
            UserTransaction utx = (UserTransaction) context.lookup("java:comp/UserTransaction");
            utx.begin();
            
            try {
                // Operations on multiple XA resources
                dataSource1.getConnection().execute("UPDATE account...");
                dataSource2.getConnection().execute("UPDATE inventory...");
                
                utx.commit(); // 2PC commit across all resources
            } catch (Exception e) {
                utx.rollback(); // 2PC rollback
            }
            """);
        
        return response;
    }
}
