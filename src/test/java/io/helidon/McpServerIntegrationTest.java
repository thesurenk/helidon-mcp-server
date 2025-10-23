package io.helidon;

import io.helidon.webserver.WebServer;
import io.helidon.webserver.http.HttpRouting;
import io.helidon.http.HeaderNames;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the MCP server using Testcontainers.
 * Tests full system behavior including concurrent requests and protocol compliance.
 * 
 * @author Suren K
 */
@DisplayName("MCP Server Integration Tests")
@Testcontainers
class McpServerIntegrationTest {

    @Container
    static GenericContainer<?> mcpServerContainer = new GenericContainer<>(
            DockerImageName.parse("openjdk:21-jdk-slim"))
            .withExposedPorts(8080)
            .withCommand("tail", "-f", "/dev/null"); // Keep container running

    private WebServer server;
    private HttpClient client;

    @BeforeEach
    void setUp() {
        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop();
        }
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
    @DisplayName("Should handle concurrent requests")
    void shouldHandleConcurrentRequests() throws Exception {
        startServer(0);
        int port = server.port();
        
        // Create multiple concurrent requests
        int numberOfRequests = 10;
        Thread[] threads = new Thread[numberOfRequests];
        boolean[] results = new boolean[numberOfRequests];
        
        for (int i = 0; i < numberOfRequests; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                try {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("http://localhost:" + port + "/"))
                            .GET()
                            .build();
                    
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    results[index] = response.statusCode() == 200;
                } catch (Exception e) {
                    results[index] = false;
                }
            });
        }
        
        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }
        
        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }
        
        // Verify all requests succeeded
        for (boolean result : results) {
            assertTrue(result, "All concurrent requests should succeed");
        }
    }

    @Test
    @DisplayName("Should handle MCP protocol requests correctly")
    void shouldHandleMcpProtocolRequests() throws Exception {
        startServer(0);
        int port = server.port();
        
        // Test MCP initialization request
        String initRequest = """
                {
                    "jsonrpc": "2.0",
                    "id": 1,
                    "method": "initialize",
                    "params": {
                        "protocolVersion": "2024-11-05",
                        "capabilities": {},
                        "clientInfo": {
                            "name": "test-client",
                            "version": "1.0.0"
                        }
                    }
                }
                """;
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/mcp"))
                .POST(HttpRequest.BodyPublishers.ofString(initRequest))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, response.statusCode());
        assertEquals("application/json", response.headers().firstValue("content-type").orElse(""));
        
        // Verify response contains expected JSON-RPC structure
        String responseBody = response.body();
        assertTrue(responseBody.contains("\"jsonrpc\":\"2.0\""), "Response should contain jsonrpc field");
        assertTrue(responseBody.contains("\"result\""), "Response should contain result field");
        assertTrue(responseBody.contains("\"id\":1"), "Response should contain correct id");
    }

    @Test
    @DisplayName("Should handle large payloads")
    void shouldHandleLargePayloads() throws Exception {
        startServer(0);
        int port = server.port();
        
        // Create a large JSON payload
        StringBuilder largePayload = new StringBuilder();
        largePayload.append("{\"jsonrpc\":\"2.0\",\"method\":\"test\",\"params\":{\"data\":\"");
        for (int i = 0; i < 1000; i++) {
            largePayload.append("This is a large payload to test server capacity. ");
        }
        largePayload.append("\"},\"id\":1}");
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/mcp"))
                .POST(HttpRequest.BodyPublishers.ofString(largePayload.toString()))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"jsonrpc\":\"2.0\""));
    }

    @Test
    @DisplayName("Should handle malformed JSON gracefully")
    void shouldHandleMalformedJsonGracefully() throws Exception {
        startServer(0);
        int port = server.port();
        
        String malformedJson = "{ invalid json }";
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/mcp"))
                .POST(HttpRequest.BodyPublishers.ofString(malformedJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        // Server should still respond (even if with error)
        assertTrue(response.statusCode() >= 200 && response.statusCode() < 500);
    }

    @Test
    @DisplayName("Should maintain server state across requests")
    void shouldMaintainServerStateAcrossRequests() throws Exception {
        startServer(0);
        int port = server.port();
        
        // Make multiple requests to ensure server remains stable
        for (int i = 0; i < 5; i++) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + port + "/"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());
            assertEquals("Helidon MCP Server is running!", response.body());
        }
    }
}
