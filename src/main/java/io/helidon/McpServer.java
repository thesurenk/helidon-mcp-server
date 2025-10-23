package io.helidon;

import io.helidon.webserver.WebServer;
import io.helidon.webserver.http.HttpRouting;
import io.helidon.http.HeaderNames;

/**
 * Main MCP (Model Context Protocol) server implementation using Helidon framework.
 * This server provides HTTP endpoints for MCP protocol communication.
 * 
 * @author Suren K
 */
public class McpServer {
    public static void main(String[] args) {
        WebServer.builder()
            .routing(HttpRouting.builder()
                .get("/", (req, res) -> res.send("Helidon MCP Server is running!"))
                .get("/mcp", (req, res) -> res.send("MCP endpoint available"))
                .post("/mcp", (req, res) -> {
                    // Basic MCP protocol handler
                    res.headers().add(HeaderNames.CONTENT_TYPE, "application/json");
                    res.send("{\"jsonrpc\":\"2.0\",\"result\":\"MCP server response\",\"id\":1}");
                }))
            .port(8080)
            .build()
            .start();
    }
}