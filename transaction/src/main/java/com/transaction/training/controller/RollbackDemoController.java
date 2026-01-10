package com.transaction.training.controller;

import com.transaction.training.dto.DemoResponse;
import com.transaction.training.service.RollbackDemoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/demo/spring/rollback")
@RequiredArgsConstructor
@Tag(name = "Spring Transaction Rollback", description = "Transaction rollback rules and pitfalls demonstrations")
@CrossOrigin(origins = "*")
public class RollbackDemoController {
    
    private final RollbackDemoService rollbackDemoService;
    
    @PostMapping("/default")
    @Operation(summary = "Demonstrate default rollback behavior")
    public DemoResponse demonstrateDefaultRollback(@RequestParam(defaultValue = "false") boolean throwException) {
        return rollbackDemoService.demonstrateDefaultRollback(throwException);
    }
    
    @PostMapping("/checked-exception")
    @Operation(summary = "Demonstrate checked exception behavior (no rollback)")
    public DemoResponse demonstrateCheckedExceptionNoRollback() {
        return rollbackDemoService.demonstrateCheckedExceptionNoRollback();
    }
    
    @PostMapping("/rollback-for")
    @Operation(summary = "Demonstrate rollbackFor attribute")
    public DemoResponse demonstrateRollbackFor() {
        return rollbackDemoService.demonstrateRollbackFor();
    }
    
    @PostMapping("/no-rollback-for")
    @Operation(summary = "Demonstrate noRollbackFor attribute")
    public DemoResponse demonstrateNoRollbackFor(@RequestParam(defaultValue = "false") boolean throwException) {
        return rollbackDemoService.demonstrateNoRollbackFor(throwException);
    }
    
    @GetMapping("/pitfalls/self-invocation")
    @Operation(summary = "Demonstrate self-invocation pitfall")
    public DemoResponse demonstrateSelfInvocation() {
        return rollbackDemoService.demonstrateSelfInvocation();
    }
    
    @GetMapping("/pitfalls/transaction-boundary")
    @Operation(summary = "Demonstrate transaction boundary issues")
    public DemoResponse demonstrateTransactionBoundary() {
        return rollbackDemoService.demonstrateTransactionBoundary();
    }
}
