package io.helidon;

import io.helidon.webserver.WebServer;
import io.helidon.webserver.http.HttpRouting;
import io.helidon.http.HeaderNames;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the MCP server implementation.
 * Tests individual components and endpoints in isolation.
 * 
 * @author Suren K
 */
@DisplayName("MCP Server Unit Tests")
class McpServerTest {

    private WebServer server;
    private HttpClient client;

    @BeforeEach
    void setUp() {
        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
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
    @DisplayName("Should start server successfully")
    void shouldStartServer() {
        assertDoesNotThrow(() -> {
            startServer(0); // Use random port
            assertTrue(server.isRunning());
        });
    }

    @Test
    @DisplayName("Should respond to root endpoint")
    void shouldRespondToRootEndpoint() throws Exception {
        startServer(0);
        int port = server.port();
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, response.statusCode());
        assertEquals("Helidon MCP Server is running!", response.body());
    }

    @Test
    @DisplayName("Should respond to MCP GET endpoint")
    void shouldRespondToMcpGetEndpoint() throws Exception {
        startServer(0);
        int port = server.port();
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/mcp"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, response.statusCode());
        assertEquals("MCP endpoint available", response.body());
    }

    @Test
    @DisplayName("Should respond to MCP POST endpoint with JSON")
    void shouldRespondToMcpPostEndpoint() throws Exception {
        startServer(0);
        int port = server.port();
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/mcp"))
                .POST(HttpRequest.BodyPublishers.ofString("{\"jsonrpc\":\"2.0\",\"method\":\"test\",\"id\":1}"))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, response.statusCode());
        assertEquals("application/json", response.headers().firstValue("content-type").orElse(""));
        assertTrue(response.body().contains("\"jsonrpc\":\"2.0\""));
        assertTrue(response.body().contains("\"result\":\"MCP server response\""));
        assertTrue(response.body().contains("\"id\":1"));
    }

    @Test
    @DisplayName("Should handle 404 for unknown endpoints")
    void shouldHandle404ForUnknownEndpoints() throws Exception {
        startServer(0);
        int port = server.port();
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/unknown"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(404, response.statusCode());
    }

    @Test
    @DisplayName("Should use correct port")
    void shouldUseCorrectPort() {
        int expectedPort = 8080;
        startServer(expectedPort);
        
        assertEquals(expectedPort, server.port());
    }
}
