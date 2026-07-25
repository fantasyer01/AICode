package com.simulator.service;

import com.simulator.model.SimulationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class DatabaseService {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseService.class);

    private final JdbcTemplate jdbcTemplate;
    private final AtomicBoolean continuousTestRunning = new AtomicBoolean(false);
    private final List<SimulationResult> testResults = new CopyOnWriteArrayList<>();

    public DatabaseService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public SimulationResult executeTestQuery() {
        long startTime = System.currentTimeMillis();
        try {
            List<Map<String, Object>> result = jdbcTemplate.queryForList("SELECT 1 as test_val, NOW() as server_time");
            long responseTime = System.currentTimeMillis() - startTime;
            
            SimulationResult simResult = SimulationResult.success("TEST_QUERY", "Query executed successfully");
            simResult.setResponseTimeMs(responseTime);
            simResult.setDetails("Result: " + result.toString());
            
            logger.info("Test query succeeded in {}ms", responseTime);
            return simResult;
            
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            SimulationResult simResult = SimulationResult.failure("TEST_QUERY", "Query failed: " + e.getMessage());
            simResult.setResponseTimeMs(responseTime);
            
            logger.error("Test query failed after {}ms: {}", responseTime, e.getMessage());
            return simResult;
        }
    }

    public SimulationResult executeDataQuery() {
        long startTime = System.currentTimeMillis();
        try {
            List<Map<String, Object>> result = jdbcTemplate.queryForList("SELECT * FROM test_data LIMIT 10");
            long responseTime = System.currentTimeMillis() - startTime;
            
            SimulationResult simResult = SimulationResult.success("DATA_QUERY", "Found " + result.size() + " records");
            simResult.setResponseTimeMs(responseTime);
            simResult.setDetails("Records: " + result.toString());
            
            logger.info("Data query succeeded in {}ms, returned {} rows", responseTime, result.size());
            return simResult;
            
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            SimulationResult simResult = SimulationResult.failure("DATA_QUERY", "Query failed: " + e.getMessage());
            simResult.setResponseTimeMs(responseTime);
            
            logger.error("Data query failed after {}ms: {}", responseTime, e.getMessage());
            return simResult;
        }
    }

    public SimulationResult insertTestData() {
        long startTime = System.currentTimeMillis();
        try {
            String name = "test_" + System.currentTimeMillis();
            String value = "Value created at " + java.time.LocalDateTime.now();
            
            jdbcTemplate.update("INSERT INTO test_data (name, value) VALUES (?, ?)", name, value);
            long responseTime = System.currentTimeMillis() - startTime;
            
            SimulationResult simResult = SimulationResult.success("INSERT", "Data inserted successfully");
            simResult.setResponseTimeMs(responseTime);
            simResult.setDetails("Inserted: name=" + name);
            
            logger.info("Insert succeeded in {}ms", responseTime);
            return simResult;
            
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            SimulationResult simResult = SimulationResult.failure("INSERT", "Insert failed: " + e.getMessage());
            simResult.setResponseTimeMs(responseTime);
            
            logger.error("Insert failed after {}ms: {}", responseTime, e.getMessage());
            return simResult;
        }
    }

    public void startContinuousTest(int intervalMs) {
        if (continuousTestRunning.compareAndSet(false, true)) {
            testResults.clear();
            Thread testThread = new Thread(() -> {
                logger.info("Starting continuous test with interval {}ms", intervalMs);
                while (continuousTestRunning.get()) {
                    SimulationResult result = executeTestQuery();
                    testResults.add(result);
                    
                    // Keep only last 100 results
                    if (testResults.size() > 100) {
                        testResults.remove(0);
                    }
                    
                    try {
                        Thread.sleep(intervalMs);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                logger.info("Continuous test stopped");
            });
            testThread.setDaemon(true);
            testThread.setName("ContinuousTestThread");
            testThread.start();
        }
    }

    public void stopContinuousTest() {
        continuousTestRunning.set(false);
        logger.info("Stopping continuous test");
    }

    public boolean isContinuousTestRunning() {
        return continuousTestRunning.get();
    }

    public List<SimulationResult> getContinuousTestResults() {
        return new ArrayList<>(testResults);
    }

    public void clearTestResults() {
        testResults.clear();
    }
}
