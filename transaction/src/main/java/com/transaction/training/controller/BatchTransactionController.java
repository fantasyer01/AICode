package com.transaction.training.controller;

import com.transaction.training.dto.DemoResponse;
import com.transaction.training.service.BatchTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/demo/spring/batch")
@RequiredArgsConstructor
@Tag(name = "Batch Transactions", description = "Spring batch transaction demonstrations")
@CrossOrigin(origins = "*")
public class BatchTransactionController {
    
    private final BatchTransactionService batchTransactionService;
    
    @PostMapping("/success")
    @Operation(summary = "Demonstrate successful batch transaction")
    public DemoResponse demonstrateBatchSuccess() {
        return batchTransactionService.demonstrateBatchSuccess();
    }
    
    @PostMapping("/rollback")
    @Operation(summary = "Demonstrate batch transaction rollback on error")
    public DemoResponse demonstrateBatchRollback() {
        return batchTransactionService.demonstrateBatchRollback();
    }
}
