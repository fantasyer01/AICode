package com.example.httpclient.test;

import com.example.httpclient.test.client.ConnectionPoolConfig;
import com.example.httpclient.test.client.HttpClientManager;
import com.example.httpclient.test.executor.RequestExecutor;
import com.example.httpclient.test.monitor.ConnectionMonitor;
import com.example.httpclient.test.orchestrator.TestOrchestrator;
import com.example.httpclient.test.server.LocalHttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main application entry point for HttpClient idle connection eviction testing
 */
public class Application {
    
    private static final Logger logger = LoggerFactory.getLogger(Application.class);
    
    private static final int SERVER_PORT = 8080;
    private static final int SERVER_RESPONSE_DELAY = 100; // ms
    private static final int MONITOR_INTERVAL = 3; // seconds
    private static final String BASE_URL = "http://localhost:" + SERVER_PORT;

    public static void main(String[] args) {
        logger.info("========================================");
        logger.info("Apache HttpClient Idle Connection Eviction Test");
        logger.info("========================================");
        
        LocalHttpServer server = null;
        HttpClientManager clientManager = null;
        ConnectionMonitor monitor = null;
        RequestExecutor executor = null;
        
        try {
            // Step 1: Start local HTTP server
            logger.info("Step 1: Starting local HTTP server on port {}...", SERVER_PORT);
            server = new LocalHttpServer(SERVER_PORT, SERVER_RESPONSE_DELAY);
            server.start();
            Thread.sleep(1000); // Give server time to start
            
            // Step 2: Configure and initialize HttpClient
            logger.info("Step 2: Configuring HttpClient with connection pool...");
            ConnectionPoolConfig poolConfig = new ConnectionPoolConfig();
            logger.info("Connection Pool Configuration:");
            logger.info("  {}", poolConfig);
            
            clientManager = new HttpClientManager(poolConfig);
            clientManager.initialize();
            
            // Step 3: Start connection monitoring
            logger.info("Step 3: Starting connection pool monitoring...");
            monitor = new ConnectionMonitor(
                clientManager.getConnectionManager(), 
                MONITOR_INTERVAL
            );
            monitor.start();
            
            // Step 4: Create request executor
            logger.info("Step 4: Creating request executor...");
            executor = new RequestExecutor(
                clientManager.getHttpClient(), 
                BASE_URL
            );
            
            // Step 5: Execute test scenarios
            logger.info("Step 5: Executing test scenarios...");
            TestOrchestrator orchestrator = new TestOrchestrator(
                clientManager, 
                executor, 
                monitor
            );
            
            // Check command line arguments for specific scenario
            if (args.length > 0) {
                try {
                    int scenarioNumber = Integer.parseInt(args[0]);
                    logger.info("Running scenario #{} only", scenarioNumber);
                    orchestrator.executeScenario(scenarioNumber);
                } catch (NumberFormatException e) {
                    logger.error("Invalid scenario number: {}", args[0]);
                    logger.info("Usage: java -jar httpclient-idle-connection-test.jar [scenario_number]");
                    logger.info("Available scenarios: 1, 2, 3, 4");
                    logger.info("Running all scenarios by default...");
                    orchestrator.executeAllScenarios();
                }
            } else {
                orchestrator.executeAllScenarios();
            }
            
            // Wait a bit to observe final state
            logger.info("Waiting 10 seconds to observe final state...");
            Thread.sleep(10000);
            
            logger.info("");
            logger.info("========================================");
            logger.info("Test Execution Summary");
            logger.info("========================================");
            logger.info(monitor.getDetailedStats());
            logger.info("========================================");
            
        } catch (Exception e) {
            logger.error("Error during test execution", e);
            
        } finally {
            // Cleanup
            logger.info("Cleaning up resources...");
            
            if (monitor != null) {
                monitor.stop();
            }
            
            if (executor != null) {
                executor.shutdown();
            }
            
            if (clientManager != null) {
                clientManager.close();
            }
            
            if (server != null) {
                server.stop();
            }
            
            logger.info("Application completed successfully");
        }
    }
}
