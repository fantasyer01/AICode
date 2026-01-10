package com.transaction.training.controller;

import com.transaction.training.dto.DatabaseInfo;
import com.transaction.training.service.DatabaseManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/database")
@RequiredArgsConstructor
@Tag(name = "Database Management", description = "Database profile management and connection status")
@CrossOrigin(origins = "*")
public class DatabaseController {
    
    private final DatabaseManagementService databaseManagementService;
    
    @GetMapping("/profiles")
    @Operation(summary = "Get available database profiles")
    public List<String> getProfiles() {
        return databaseManagementService.getAvailableProfiles();
    }
    
    @GetMapping("/active")
    @Operation(summary = "Get current active database information")
    public DatabaseInfo getActiveDatabase() {
        return databaseManagementService.getActiveDatabaseInfo();
    }
    
    @PostMapping("/switch")
    @Operation(summary = "Switch to different database profile")
    public Map<String, Object> switchDatabase(@RequestBody Map<String, String> request) {
        String profile = request.get("profile");
        boolean success = databaseManagementService.switchDatabase(profile);
        return Map.of(
            "success", success,
            "profile", profile,
            "message", success ? "Database switched successfully" : "Failed to switch database"
        );
    }
    
    @GetMapping("/status")
    @Operation(summary = "Check database connection status")
    public Map<String, Object> getDatabaseStatus() {
        return databaseManagementService.checkDatabaseStatus();
    }
}
