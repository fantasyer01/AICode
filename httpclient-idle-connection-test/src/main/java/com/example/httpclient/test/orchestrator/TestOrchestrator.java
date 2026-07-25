package com.example.httpclient.test.orchestrator;

import com.example.httpclient.test.client.HttpClientManager;
import com.example.httpclient.test.executor.RequestExecutor;
import com.example.httpclient.test.monitor.ConnectionMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Coordinates test scenarios and validates eviction behavior
 */
public class TestOrchestrator {
    
    private static final Logger logger = LoggerFactory.getLogger(TestOrchestrator.class);
    
    private final HttpClientManager clientManager;
    private final RequestExecutor requestExecutor;
    private final ConnectionMonitor connectionMonitor;

    public TestOrchestrator(HttpClientManager clientManager, 
                           RequestExecutor requestExecutor, 
                           ConnectionMonitor connectionMonitor) {
        this.clientManager = clientManager;
        this.requestExecutor = requestExecutor;
        this.connectionMonitor = connectionMonitor;
    }

    /**
     * Execute all test scenarios
     */
    public void executeAllScenarios() {
        logger.info("========================================");
        logger.info("Starting All Test Scenarios");
        logger.info("========================================");
        
        executeScenario1();
        waitBetweenScenarios();
        
        executeScenario2();
        waitBetweenScenarios();
        
        executeScenario3();
        waitBetweenScenarios();
        
        executeScenario4();
        
        logger.info("========================================");
        logger.info("All Test Scenarios Completed");
        logger.info("========================================");
    }

    /**
     * Scenario 1: Basic Connection Pooling
     * - Execute sequential requests
     * - Verify connections are returned to pool
     * - Confirm connection reuse
     */
    public void executeScenario1() {
        logger.info("");
        logger.info("========================================");
        logger.info("Scenario 1: Basic Connection Pooling");
        logger.info("========================================");
        
        connectionMonitor.logPoolStatistics("Before requests");
        
        logger.info("Executing 5 sequential requests...");
        requestExecutor.executeSequentialRequests(5);
        
        sleep(2000);
        connectionMonitor.logPoolStatistics("After requests");
        
        logger.info("Scenario 1 completed - Connections should be returned to pool");
    }

    /**
     * Scenario 2: Idle Connection Eviction
     * - Execute concurrent requests to fill the pool
     * - Wait for idle timeout period
     * - Verify idle connections are evicted
     */
    public void executeScenario2() {
        logger.info("");
        logger.info("========================================");
        logger.info("Scenario 2: Idle Connection Eviction");
        logger.info("========================================");
        
        connectionMonitor.logPoolStatistics("Before concurrent requests");
        
        logger.info("Executing 10 concurrent requests to populate pool...");
        requestExecutor.executeConcurrentRequests(10);
        
        sleep(2000);
        connectionMonitor.logPoolStatistics("After concurrent requests");
        
        logger.info("Waiting 15 seconds for idle connection eviction...");
        sleep(15000);
        
        connectionMonitor.logPoolStatistics("After idle period");
        
        logger.info("Scenario 2 completed - Idle connections should be evicted");
    }

    /**
     * Scenario 3: Connection Time-To-Live
     * - Execute requests continuously
     * - Hold connections for longer than TTL
     * - Verify connections are replaced after TTL expiration
     */
    public void executeScenario3() {
        logger.info("");
        logger.info("========================================");
        logger.info("Scenario 3: Connection Time-To-Live");
        logger.info("========================================");
        
        connectionMonitor.logPoolStatistics("Before TTL test");
        
        logger.info("Executing requests over time to test TTL (30 seconds)...");
        requestExecutor.executeRequestsWithDelay(6, 6000);
        
        connectionMonitor.logPoolStatistics("After TTL period");
        
        logger.info("Waiting additional time for TTL expiration...");
        sleep(5000);
        
        connectionMonitor.logPoolStatistics("After TTL expiration wait");
        
        logger.info("Scenario 3 completed - Connections should be replaced after TTL");
    }

    /**
     * Scenario 4: Manual Eviction Trigger
     * - Populate connection pool with requests
     * - Manually trigger eviction
     * - Observe immediate cleanup effect
     */
    public void executeScenario4() {
        logger.info("");
        logger.info("========================================");
        logger.info("Scenario 4: Manual Eviction Trigger");
        logger.info("========================================");
        
        connectionMonitor.logPoolStatistics("Before manual eviction test");
        
        logger.info("Populating pool with 8 concurrent requests...");
        requestExecutor.executeConcurrentRequests(8);
        
        sleep(2000);
        connectionMonitor.logPoolStatistics("After populating pool");
        
        // logger.info("Waiting 12 seconds to make connections idle...");
        logger.info("Waiting 3 seconds to make connections idle...");
        sleep(3000);
        
        connectionMonitor.logPoolStatistics("Before manual eviction");
        
        logger.info("Manually triggering closeExpiredConnections()...");
        clientManager.closeExpiredConnections();
        
        sleep(1000);
        connectionMonitor.logPoolStatistics("After closeExpiredConnections()");
        
        logger.info("Manually triggering closeIdleConnections(10 seconds)...");
        clientManager.closeIdleConnections(10, TimeUnit.SECONDS);
        
        sleep(1000);
        connectionMonitor.logPoolStatistics("After closeIdleConnections()");
        
        logger.info("Scenario 4 completed - Manual eviction should clean up idle connections");
    }

    /**
     * Execute a specific scenario by number
     */
    public void executeScenario(int scenarioNumber) {
        switch (scenarioNumber) {
            case 1:
                executeScenario1();
                break;
            case 2:
                executeScenario2();
                break;
            case 3:
                executeScenario3();
                break;
            case 4:
                executeScenario4();
                break;
            default:
                logger.error("Invalid scenario number: {}", scenarioNumber);
        }
    }

    /**
     * Sleep helper method
     */
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Sleep interrupted");
        }
    }

    /**
     * Wait between scenarios
     */
    private void waitBetweenScenarios() {
        logger.info("Waiting 5 seconds before next scenario...");
        sleep(5000);
    }
}
