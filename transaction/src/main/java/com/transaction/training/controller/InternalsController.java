package com.transaction.training.controller;

import com.transaction.training.dto.DemoResponse;
import com.transaction.training.service.DatabaseInternalsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/demo/internals")
@RequiredArgsConstructor
@Tag(name = "Database Internals", description = "Database internal mechanism demonstrations")
@CrossOrigin(origins = "*")
public class InternalsController {
    
    private final DatabaseInternalsService internalsService;
    
    @PostMapping("/redolog")
    @Operation(summary = "Demonstrate Redo Log mechanism")
    public DemoResponse demonstrateRedoLog() {
        return internalsService.demonstrateRedoLog();
    }
    
    @PostMapping("/undolog")
    @Operation(summary = "Demonstrate Undo Log mechanism")
    public DemoResponse demonstrateUndoLog() {
        return internalsService.demonstrateUndoLog();
    }
    
    @PostMapping("/mvcc")
    @Operation(summary = "Demonstrate MVCC mechanism")
    public DemoResponse demonstrateMVCC() {
        return internalsService.demonstrateMVCC();
    }
    
    @PostMapping("/wal")
    @Operation(summary = "Demonstrate WAL protocol")
    public DemoResponse demonstrateWAL() {
        return internalsService.demonstrateWAL();
    }
    
    @PostMapping("/locks")
    @Operation(summary = "Demonstrate lock mechanisms")
    public DemoResponse demonstrateLocks() {
        return internalsService.demonstrateLocks();
    }
}
