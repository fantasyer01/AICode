package com.transaction.training.controller;

import com.transaction.training.dto.DemoResponse;
import com.transaction.training.service.DistributedTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/demo/distributed")
@RequiredArgsConstructor
@Tag(name = "Distributed Transactions", description = "Distributed transaction pattern demonstrations")
@CrossOrigin(origins = "*")
public class DistributedTransactionController {
    
    private final DistributedTransactionService distributedTransactionService;
    
    @PostMapping("/seata/at")
    @Operation(summary = "Demonstrate Seata AT mode")
    public DemoResponse demonstrateSeataAT() {
        return distributedTransactionService.demonstrateSeataAT();
    }
    
    @PostMapping("/seata/tcc")
    @Operation(summary = "Demonstrate Seata TCC mode")
    public DemoResponse demonstrateSeataTCC() {
        return distributedTransactionService.demonstrateSeataTCC();
    }
    
    @PostMapping("/seata/saga")
    @Operation(summary = "Demonstrate Seata SAGA mode")
    public DemoResponse demonstrateSeataSAGA() {
        return distributedTransactionService.demonstrateSeataSAGA();
    }
    
    @PostMapping("/pattern/2pc")
    @Operation(summary = "Demonstrate 2PC pattern")
    public DemoResponse demonstrate2PC() {
        return distributedTransactionService.demonstrate2PC();
    }
}
