package com.transaction.training.controller;

import com.transaction.training.dto.DemoResponse;
import com.transaction.training.service.PropagationDemoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/demo/spring/propagation")
@RequiredArgsConstructor
@Tag(name = "Spring Transaction Propagation", description = "Demonstration of transaction propagation behaviors")
@CrossOrigin(origins = "*")
public class PropagationDemoController {
    
    private final PropagationDemoService propagationDemoService;
    
    @PostMapping("/required")
    @Operation(summary = "Demonstrate REQUIRED propagation")
    public DemoResponse demonstrateRequired() {
        return propagationDemoService.demonstrateRequired();
    }
    
    @PostMapping("/requires-new")
    @Operation(summary = "Demonstrate REQUIRES_NEW propagation")
    public DemoResponse demonstrateRequiresNew() {
        return propagationDemoService.demonstrateRequiresNew();
    }
    
    @PostMapping("/nested")
    @Operation(summary = "Demonstrate NESTED propagation")
    public DemoResponse demonstrateNested() {
        return propagationDemoService.demonstrateNested();
    }
    
    @PostMapping("/supports")
    @Operation(summary = "Demonstrate SUPPORTS propagation")
    public DemoResponse demonstrateSupports() {
        return propagationDemoService.demonstrateSupports();
    }
    
    @PostMapping("/not-supported")
    @Operation(summary = "Demonstrate NOT_SUPPORTED propagation")
    public DemoResponse demonstrateNotSupported() {
        return propagationDemoService.demonstrateNotSupported();
    }
    
    @PostMapping("/mandatory")
    @Operation(summary = "Demonstrate MANDATORY propagation")
    public DemoResponse demonstrateMandatory() {
        return propagationDemoService.demonstrateMandatory();
    }
    
    @PostMapping("/never")
    @Operation(summary = "Demonstrate NEVER propagation")
    public DemoResponse demonstrateNever() {
        return propagationDemoService.demonstrateNever();
    }
    
    @GetMapping("/compare")
    @Operation(summary = "Compare all propagation behaviors")
    public DemoResponse comparePropagations() {
        return propagationDemoService.comparePropagations();
    }
}
