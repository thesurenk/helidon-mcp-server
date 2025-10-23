package io.helidon;

import io.helidon.webserver.WebServer;
import io.helidon.webserver.http.HttpRouting;
import io.helidon.http.HeaderNames;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Disabled;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CompletableFuture;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Performance tests for the MCP server.
 * Tests server performance under load, memory efficiency, and sustained throughput.
 * 
 * @author Suren K
 */
@DisplayName("MCP Server Performance Tests")
class McpServerPerformanceTest {

    private WebServer server;
    private ExecutorService executor;

    @BeforeEach
    void setUp() {
        executor = TestUtils.createTestExecutor(20);
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop();
        }
        TestUtils.shutdownExecutor(executor);
    }

    private void startServer(int port) {
        server = WebServer.builder()
                .routing(HttpRouting.builder()
                        .get("/", (req, res) -> res.send("Helidon MCP Server is running!"))
                        .get("/mcp", (req, res) -> res.send("MCP endpoint available"))
                        .post("/mcp", (req, res) -> {
                            res.headers().add(HeaderNames.CONTENT_TYPE, "application/json");
                            res.send("{\"jsonrpc\":\"2.0\",\"result\":\"MCP server response\",\"id\":1}");
                        }))
                .port(port)
                .build()
                .start();
    }

    @Test
    @DisplayName("Should handle high throughput requests")
    @Disabled("Performance test - run manually when needed")
    void shouldHandleHighThroughputRequests() throws Exception {
        startServer(0);
        int port = server.port();
        String baseUrl = "http://localhost:" + port;
        
        int numberOfRequests = 1000;
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < numberOfRequests; i++) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    HttpRequest request = TestUtils.createGetRequest(baseUrl + "/");
                    HttpResponse<String> response = TestUtils.sendRequest(request);
                    if (response.statusCode() == 200) {
                        successCount.incrementAndGet();
                    } else {
                        errorCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                }
            }, executor);
            futures.add(future);
        }
        
        // Wait for all requests to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(30, TimeUnit.SECONDS);
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.println("Performance Test Results:");
        System.out.println("Total requests: " + numberOfRequests);
        System.out.println("Successful requests: " + successCount.get());
        System.out.println("Failed requests: " + errorCount.get());
        System.out.println("Duration: " + duration + "ms");
        System.out.println("Requests per second: " + (numberOfRequests * 1000.0 / duration));
        
        // Assert that most requests succeeded
        assertTrue(successCount.get() > numberOfRequests * 0.95, 
                "At least 95% of requests should succeed");
        assertTrue(duration < 30000, "All requests should complete within 30 seconds");
    }

    @Test
    @DisplayName("Should handle sustained load")
    @Disabled("Performance test - run manually when needed")
    void shouldHandleSustainedLoad() throws Exception {
        startServer(0);
        int port = server.port();
        String baseUrl = "http://localhost:" + port;
        
        int durationSeconds = 10;
        int requestsPerSecond = 50;
        AtomicInteger totalRequests = new AtomicInteger(0);
        AtomicInteger successCount = new AtomicInteger(0);
        
        long startTime = System.currentTimeMillis();
        long endTime = startTime + (durationSeconds * 1000);
        
        while (System.currentTimeMillis() < endTime) {
            List<CompletableFuture<Void>> batch = new ArrayList<>();
            
            // Send a batch of requests
            for (int i = 0; i < requestsPerSecond; i++) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        HttpRequest request = TestUtils.createGetRequest(baseUrl + "/");
                        HttpResponse<String> response = TestUtils.sendRequest(request);
                        totalRequests.incrementAndGet();
                        if (response.statusCode() == 200) {
                            successCount.incrementAndGet();
                        }
                    } catch (Exception e) {
                        totalRequests.incrementAndGet();
                    }
                }, executor);
                batch.add(future);
            }
            
            // Wait for batch to complete
            CompletableFuture.allOf(batch.toArray(new CompletableFuture[0])).get();
            
            // Wait for next second
            Thread.sleep(1000);
        }
        
        System.out.println("Sustained Load Test Results:");
        System.out.println("Duration: " + durationSeconds + " seconds");
        System.out.println("Total requests: " + totalRequests.get());
        System.out.println("Successful requests: " + successCount.get());
        System.out.println("Average requests per second: " + (totalRequests.get() / durationSeconds));
        
        assertTrue(successCount.get() > totalRequests.get() * 0.9, 
                "At least 90% of requests should succeed under sustained load");
    }

    @Test
    @DisplayName("Should handle memory efficiently")
    void shouldHandleMemoryEfficiently() throws Exception {
        startServer(0);
        int port = server.port();
        String baseUrl = "http://localhost:" + port;
        
        // Get initial memory usage
        Runtime runtime = Runtime.getRuntime();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        
        // Send many requests to test memory usage
        int numberOfRequests = 100;
        for (int i = 0; i < numberOfRequests; i++) {
            HttpRequest request = TestUtils.createGetRequest(baseUrl + "/");
            HttpResponse<String> response = TestUtils.sendRequest(request);
            assertEquals(200, response.statusCode());
            
            // Force garbage collection every 20 requests
            if (i % 20 == 0) {
                System.gc();
            }
        }
        
        // Get final memory usage
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryIncrease = finalMemory - initialMemory;
        
        System.out.println("Memory Test Results:");
        System.out.println("Initial memory: " + (initialMemory / 1024 / 1024) + " MB");
        System.out.println("Final memory: " + (finalMemory / 1024 / 1024) + " MB");
        System.out.println("Memory increase: " + (memoryIncrease / 1024 / 1024) + " MB");
        
        // Memory increase should be reasonable (less than 50MB for 100 requests)
        assertTrue(memoryIncrease < 50 * 1024 * 1024, 
                "Memory usage should not increase excessively");
    }
}
