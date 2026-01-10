package com.transaction.training.controller;

import com.transaction.training.dto.DemoResponse;
import com.transaction.training.service.IsolationLevelDemoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/demo/spring/isolation")
@RequiredArgsConstructor
@Tag(name = "Spring Transaction Isolation", description = "Transaction isolation level demonstrations")
@CrossOrigin(origins = "*")
public class IsolationDemoController {
    
    private final IsolationLevelDemoService isolationLevelDemoService;
    
    @PostMapping("/read-uncommitted")
    @Operation(summary = "Demonstrate READ_UNCOMMITTED isolation level")
    public DemoResponse demonstrateReadUncommitted() {
        return isolationLevelDemoService.demonstrateReadUncommitted();
    }
    
    @PostMapping("/read-committed")
    @Operation(summary = "Demonstrate READ_COMMITTED isolation level")
    public DemoResponse demonstrateReadCommitted() {
        return isolationLevelDemoService.demonstrateReadCommitted();
    }
    
    @PostMapping("/repeatable-read")
    @Operation(summary = "Demonstrate REPEATABLE_READ isolation level")
    public DemoResponse demonstrateRepeatableRead() {
        return isolationLevelDemoService.demonstrateRepeatableRead();
    }
    
    @PostMapping("/serializable")
    @Operation(summary = "Demonstrate SERIALIZABLE isolation level")
    public DemoResponse demonstrateSerializable() {
        return isolationLevelDemoService.demonstrateSerializable();
    }
    
    @GetMapping("/compare")
    @Operation(summary = "Compare all isolation levels")
    public DemoResponse compareIsolationLevels() {
        return isolationLevelDemoService.compareIsolationLevels();
    }
}
