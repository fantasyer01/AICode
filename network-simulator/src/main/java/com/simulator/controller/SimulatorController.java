package com.simulator.controller;

import com.simulator.model.ContainerStatus;
import com.simulator.model.PoolStatus;
import com.simulator.model.SimulationResult;
import com.simulator.service.DatabaseService;
import com.simulator.service.DockerService;
import com.simulator.service.PoolMonitorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SimulatorController {

    private final DockerService dockerService;
    private final DatabaseService databaseService;
    private final PoolMonitorService poolMonitorService;

    public SimulatorController(DockerService dockerService, 
                               DatabaseService databaseService,
                               PoolMonitorService poolMonitorService) {
        this.dockerService = dockerService;
        this.databaseService = databaseService;
        this.poolMonitorService = poolMonitorService;
    }

    // ==================== Pool Status ====================
    
    @GetMapping("/pool/status")
    public ResponseEntity<PoolStatus> getPoolStatus() {
        return ResponseEntity.ok(poolMonitorService.getPoolStatus());
    }

    @PostMapping("/pool/reset")
    public ResponseEntity<SimulationResult> resetPoolStats() {
        poolMonitorService.resetPoolStats();
        return ResponseEntity.ok(SimulationResult.success("POOL_RESET", "Pool statistics reset"));
    }

    // ==================== Container Status ====================
    
    @GetMapping("/container/status")
    public ResponseEntity<ContainerStatus> getContainerStatus() {
        return ResponseEntity.ok(dockerService.getContainerStatus());
    }

    @GetMapping("/docker/available")
    public ResponseEntity<Map<String, Boolean>> isDockerAvailable() {
        Map<String, Boolean> result = new HashMap<>();
        result.put("available", dockerService.isDockerAvailable());
        return ResponseEntity.ok(result);
    }

    // ==================== Fault Simulation ====================
    
    @PostMapping("/fault/network-down")
    public ResponseEntity<SimulationResult> networkDown() {
        return ResponseEntity.ok(dockerService.disconnectNetwork());
    }

    @PostMapping("/fault/network-up")
    public ResponseEntity<SimulationResult> networkUp() {
        return ResponseEntity.ok(dockerService.reconnectNetwork());
    }

    @PostMapping("/fault/db-stop")
    public ResponseEntity<SimulationResult> stopDatabase() {
        return ResponseEntity.ok(dockerService.stopContainer());
    }

    @PostMapping("/fault/db-start")
    public ResponseEntity<SimulationResult> startDatabase() {
        return ResponseEntity.ok(dockerService.startContainer());
    }

    @PostMapping("/fault/db-restart")
    public ResponseEntity<SimulationResult> restartDatabase() {
        return ResponseEntity.ok(dockerService.restartContainer());
    }

    @PostMapping("/fault/timeout")
    public ResponseEntity<SimulationResult> triggerTimeout() {
        return ResponseEntity.ok(dockerService.pauseContainer());
    }

    @PostMapping("/fault/timeout-clear")
    public ResponseEntity<SimulationResult> clearTimeout() {
        return ResponseEntity.ok(dockerService.unpauseContainer());
    }

    // ==================== Database Testing ====================
    
    @PostMapping("/test/query")
    public ResponseEntity<SimulationResult> executeTestQuery() {
        return ResponseEntity.ok(databaseService.executeTestQuery());
    }

    @PostMapping("/test/data-query")
    public ResponseEntity<SimulationResult> executeDataQuery() {
        return ResponseEntity.ok(databaseService.executeDataQuery());
    }

    @PostMapping("/test/insert")
    public ResponseEntity<SimulationResult> insertTestData() {
        return ResponseEntity.ok(databaseService.insertTestData());
    }

    @PostMapping("/test/continuous/start")
    public ResponseEntity<SimulationResult> startContinuousTest(
            @RequestParam(defaultValue = "1000") int intervalMs) {
        if (databaseService.isContinuousTestRunning()) {
            return ResponseEntity.ok(SimulationResult.failure("CONTINUOUS_TEST", "Test already running"));
        }
        databaseService.startContinuousTest(intervalMs);
        return ResponseEntity.ok(SimulationResult.success("CONTINUOUS_TEST", "Continuous test started with interval " + intervalMs + "ms"));
    }

    @PostMapping("/test/continuous/stop")
    public ResponseEntity<SimulationResult> stopContinuousTest() {
        databaseService.stopContinuousTest();
        return ResponseEntity.ok(SimulationResult.success("CONTINUOUS_TEST", "Continuous test stopped"));
    }

    @GetMapping("/test/continuous/status")
    public ResponseEntity<Map<String, Object>> getContinuousTestStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("running", databaseService.isContinuousTestRunning());
        status.put("resultCount", databaseService.getContinuousTestResults().size());
        return ResponseEntity.ok(status);
    }

    @GetMapping("/test/continuous/results")
    public ResponseEntity<List<SimulationResult>> getContinuousTestResults() {
        return ResponseEntity.ok(databaseService.getContinuousTestResults());
    }

    @PostMapping("/test/continuous/clear")
    public ResponseEntity<SimulationResult> clearTestResults() {
        databaseService.clearTestResults();
        return ResponseEntity.ok(SimulationResult.success("CLEAR_RESULTS", "Test results cleared"));
    }

    // ==================== Combined Status ====================
    
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getFullStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("pool", poolMonitorService.getPoolStatus());
        status.put("container", dockerService.getContainerStatus());
        status.put("continuousTest", Map.of(
                "running", databaseService.isContinuousTestRunning(),
                "resultCount", databaseService.getContinuousTestResults().size()
        ));
        status.put("dockerAvailable", dockerService.isDockerAvailable());
        return ResponseEntity.ok(status);
    }
}
