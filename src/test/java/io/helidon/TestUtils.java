package io.helidon;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.List;
import java.util.ArrayList;

/**
 * Utility class for testing MCP server functionality.
 * Provides helper methods for HTTP requests, MCP protocol testing, and concurrent operations.
 * 
 * @author Suren K
 */
public class TestUtils {
    
    private static final HttpClient DEFAULT_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    
    /**
     * Creates a GET request to the specified URL
     */
    public static HttpRequest createGetRequest(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
    }
    
    /**
     * Creates a POST request with JSON body
     */
    public static HttpRequest createPostRequest(String url, String jsonBody) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json")
                .build();
    }
    
    /**
     * Sends a request and returns the response
     */
    public static HttpResponse<String> sendRequest(HttpRequest request) throws Exception {
        return DEFAULT_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }
    
    /**
     * Sends a request with custom client
     */
    public static HttpResponse<String> sendRequest(HttpClient client, HttpRequest request) throws Exception {
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
    
    /**
     * Creates a valid MCP initialization request
     */
    public static String createMcpInitRequest(String clientName, String clientVersion) {
        return String.format("""
                {
                    "jsonrpc": "2.0",
                    "id": 1,
                    "method": "initialize",
                    "params": {
                        "protocolVersion": "2024-11-05",
                        "capabilities": {},
                        "clientInfo": {
                            "name": "%s",
                            "version": "%s"
                        }
                    }
                }
                """, clientName, clientVersion);
    }
    
    /**
     * Creates a simple MCP request
     */
    public static String createMcpRequest(String method, Object params, int id) {
        return String.format("""
                {
                    "jsonrpc": "2.0",
                    "id": %d,
                    "method": "%s",
                    "params": %s
                }
                """, id, method, params != null ? params.toString() : "null");
    }
    
    /**
     * Validates that a response is valid JSON-RPC
     */
    public static boolean isValidJsonRpcResponse(String response) {
        return response.contains("\"jsonrpc\":\"2.0\"") && 
               (response.contains("\"result\"") || response.contains("\"error\""));
    }
    
    /**
     * Sends multiple concurrent requests
     */
    public static List<HttpResponse<String>> sendConcurrentRequests(
            String url, int numberOfRequests, ExecutorService executor) throws Exception {
        
        List<CompletableFuture<HttpResponse<String>>> futures = new ArrayList<>();
        
        for (int i = 0; i < numberOfRequests; i++) {
            HttpRequest request = createGetRequest(url);
            CompletableFuture<HttpResponse<String>> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return sendRequest(request);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, executor);
            futures.add(future);
        }
        
        List<HttpResponse<String>> responses = new ArrayList<>();
        for (CompletableFuture<HttpResponse<String>> future : futures) {
            responses.add(future.get());
        }
        
        return responses;
    }
    
    /**
     * Creates a test executor service
     */
    public static ExecutorService createTestExecutor(int threadCount) {
        return Executors.newFixedThreadPool(threadCount);
    }
    
    /**
     * Shuts down executor service gracefully
     */
    public static void shutdownExecutor(ExecutorService executor) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Waits for server to be ready
     */
    public static void waitForServer(String baseUrl, int maxAttempts, Duration delay) throws Exception {
        for (int i = 0; i < maxAttempts; i++) {
            try {
                HttpRequest request = createGetRequest(baseUrl + "/");
                HttpResponse<String> response = sendRequest(request);
                if (response.statusCode() == 200) {
                    return;
                }
            } catch (Exception e) {
                // Server not ready yet, continue waiting
            }
            Thread.sleep(delay.toMillis());
        }
        throw new RuntimeException("Server did not become ready within expected time");
    }
}
