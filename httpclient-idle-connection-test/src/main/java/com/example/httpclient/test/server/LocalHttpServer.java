package com.example.httpclient.test.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

/**
 * Local HTTP server for testing HttpClient connection pooling
 */
public class LocalHttpServer {
    
    private static final Logger logger = LoggerFactory.getLogger(LocalHttpServer.class);
    
    private final int port;
    private final int responseDelay;
    private HttpServer server;

    public LocalHttpServer(int port, int responseDelay) {
        this.port = port;
        this.responseDelay = responseDelay;
    }

    /**
     * Start the HTTP server
     */
    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/test", new TestHandler());
        server.createContext("/health", new HealthHandler());
        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();
        logger.info("Local HTTP server started on port {}", port);
    }

    /**
     * Stop the HTTP server
     */
    public void stop() {
        if (server != null) {
            server.stop(0);
            logger.info("Local HTTP server stopped");
        }
    }

    /**
     * Handler for test endpoint
     */
    private class TestHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            logger.debug("Received {} request to {}", exchange.getRequestMethod(), exchange.getRequestURI());
            
            // Simulate processing delay
            if (responseDelay > 0) {
                try {
                    Thread.sleep(responseDelay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            
            String response = String.format(
                "{\"status\":\"ok\",\"timestamp\":%d,\"message\":\"Test response\"}",
                System.currentTimeMillis()
            );
            
            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, responseBytes.length);
            
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        }
    }

    /**
     * Handler for health check endpoint
     */
    private class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "{\"status\":\"healthy\"}";
            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, responseBytes.length);
            
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        }
    }
}
