package com.transaction.training.controller;

import com.transaction.training.dto.DemoResponse;
import com.transaction.training.service.ProgrammaticTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/demo/spring/programmatic")
@RequiredArgsConstructor
@Tag(name = "Programmatic Transactions", description = "Declarative vs Programmatic transaction demonstrations")
@CrossOrigin(origins = "*")
public class ProgrammaticTransactionController {
    
    private final ProgrammaticTransactionService programmaticTransactionService;
    
    @PostMapping("/declarative")
    @Operation(summary = "Demonstrate declarative transaction (@Transactional)")
    public DemoResponse demonstrateDeclarative() {
        return programmaticTransactionService.demonstrateDeclarative();
    }
    
    @PostMapping("/transaction-template")
    @Operation(summary = "Demonstrate TransactionTemplate")
    public DemoResponse demonstrateTransactionTemplate() {
        return programmaticTransactionService.demonstrateTransactionTemplate();
    }
    
    @PostMapping("/platform-transaction-manager")
    @Operation(summary = "Demonstrate PlatformTransactionManager")
    public DemoResponse demonstratePlatformTransactionManager() {
        return programmaticTransactionService.demonstratePlatformTransactionManager();
    }
}
