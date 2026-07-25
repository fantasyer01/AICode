package com.example.httpclient.test.executor;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Executes HTTP requests to populate and exercise the connection pool
 */
public class RequestExecutor {
    
    private static final Logger logger = LoggerFactory.getLogger(RequestExecutor.class);
    
    private final CloseableHttpClient httpClient;
    private final String baseUrl;
    private final ExecutorService executorService;

    public RequestExecutor(CloseableHttpClient httpClient, String baseUrl) {
        this.httpClient = httpClient;
        this.baseUrl = baseUrl;
        this.executorService = Executors.newCachedThreadPool();
    }

    /**
     * Execute a single HTTP GET request
     */
    public boolean executeRequest(int requestId) {
        String url = baseUrl + "/test";
        HttpGet request = new HttpGet(url);
        
        try {
            logger.debug("Executing request #{} to {}", requestId, url);
            HttpResponse response = httpClient.execute(request);
            
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity());
            
            logger.debug("Request #{} completed: status={}, body={}", 
                requestId, statusCode, responseBody);
            
            EntityUtils.consume(response.getEntity());
            return statusCode == 200;
            
        } catch (IOException e) {
            logger.error("Request #{} failed: {}", requestId, e.getMessage());
            return false;
        }
    }

    /**
     * Execute multiple sequential requests
     */
    public int executeSequentialRequests(int count) {
        logger.info("Executing {} sequential requests", count);
        int successCount = 0;
        
        for (int i = 1; i <= count; i++) {
            if (executeRequest(i)) {
                successCount++;
            }
            
            // Small delay between requests
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        logger.info("Sequential requests completed: {}/{} successful", successCount, count);
        return successCount;
    }

    /**
     * Execute multiple concurrent requests
     */
    public int executeConcurrentRequests(int count) {
        logger.info("Executing {} concurrent requests", count);
        
        List<Future<Boolean>> futures = new ArrayList<>();
        
        for (int i = 1; i <= count; i++) {
            final int requestId = i;
            Future<Boolean> future = executorService.submit(() -> executeRequest(requestId));
            futures.add(future);
        }
        
        int successCount = 0;
        for (Future<Boolean> future : futures) {
            try {
                if (future.get(30, TimeUnit.SECONDS)) {
                    successCount++;
                }
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                logger.error("Error waiting for request completion: {}", e.getMessage());
            }
        }
        
        logger.info("Concurrent requests completed: {}/{} successful", successCount, count);
        return successCount;
    }

    /**
     * Execute requests with a specific delay between them
     */
    public int executeRequestsWithDelay(int count, long delayMs) {
        logger.info("Executing {} requests with {}ms delay", count, delayMs);
        int successCount = 0;
        
        for (int i = 1; i <= count; i++) {
            if (executeRequest(i)) {
                successCount++;
            }
            
            if (i < count) {
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        
        logger.info("Delayed requests completed: {}/{} successful", successCount, count);
        return successCount;
    }

    /**
     * Execute a burst of concurrent requests followed by idle period
     */
    public int executeBurstRequests(int burstSize, int burstCount, long idleMs) {
        logger.info("Executing {} bursts of {} requests with {}ms idle between bursts", 
            burstCount, burstSize, idleMs);
        
        int totalSuccess = 0;
        
        for (int burst = 1; burst <= burstCount; burst++) {
            logger.info("Executing burst #{}", burst);
            int burstSuccess = executeConcurrentRequests(burstSize);
            totalSuccess += burstSuccess;
            
            if (burst < burstCount) {
                logger.info("Idle period: {}ms", idleMs);
                try {
                    Thread.sleep(idleMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        
        logger.info("Burst requests completed: {}/{} successful", 
            totalSuccess, burstSize * burstCount);
        return totalSuccess;
    }

    /**
     * Shutdown the executor service
     */
    public void shutdown() {
        logger.info("Shutting down request executor");
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
