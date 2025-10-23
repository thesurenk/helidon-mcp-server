# Helidon MCP Server

A Model Context Protocol (MCP) server implementation using Java and Helidon framework, containerized for easy deployment.

## Quick Start

```bash
# Pull the image
docker pull surendocker/helidon-mcp-server

# Run the server
docker run -d -p 8080:8080 --name helidon-mcp-server surendocker/helidon-mcp-server

# Test the server
curl http://localhost:8080/
curl http://localhost:8080/mcp
```

## Features

- **MCP Protocol Support**: Implements the Model Context Protocol for tool discovery and execution
- **Java 21**: Built with modern Java features and Helidon 4.0.7
- **Containerized**: Ready-to-use Docker image with multi-stage build
- **HTTP Server**: RESTful API endpoints for MCP protocol communication
- **JSON-RPC Support**: Full JSON-RPC 2.0 protocol implementation

## Usage

### Basic Deployment

```bash
# Run with default settings
docker run -d -p 8080:8080 surendocker/helidon-mcp-server

# Run with custom name and restart policy
docker run -d \
  --name helidon-mcp-server \
  -p 8080:8080 \
  --restart unless-stopped \
  surendocker/helidon-mcp-server
```

### Environment Configuration

```bash
# Run with custom Java options
docker run -d \
  -p 8080:8080 \
  -e JAVA_OPTS="-Xmx512m -Xms256m" \
  surendocker/helidon-mcp-server
```

### Network Configuration

```bash
# Create custom network
docker network create mcp-network

# Run on custom network
docker run -d \
  --name helidon-mcp-server \
  --network mcp-network \
  -p 8080:8080 \
  surendocker/helidon-mcp-server
```

## API Endpoints

### Health Check
```bash
curl http://localhost:8080/
# Response: "Helidon MCP Server is running!"
```

### MCP Endpoint
```bash
curl http://localhost:8080/mcp
# Response: "MCP endpoint available"
```

### JSON-RPC Requests
```bash
# Test MCP protocol
curl -X POST http://localhost:8080/mcp \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","method":"mcp/list-tools","id":1}'

# Response: {"jsonrpc":"2.0","result":"MCP server response","id":1}
```

## Testing

### Basic Connectivity Test
```bash
# Test server is running
curl -f http://localhost:8080/ || echo "Server not responding"

# Test MCP endpoint
curl -f http://localhost:8080/mcp || echo "MCP endpoint not responding"
```

### JSON-RPC Protocol Test
```bash
# Test MCP list-tools
curl -X POST http://localhost:8080/mcp \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","method":"mcp/list-tools","id":1}' \
  -w "\nHTTP Status: %{http_code}\nResponse Time: %{time_total}s\n"
```

### Performance Test
```bash
# Concurrent request testing
for i in {1..10}; do
  curl -X POST http://localhost:8080/mcp \
    -H "Content-Type: application/json" \
    -d "{\"jsonrpc\":\"2.0\",\"method\":\"mcp/ping\",\"id\":$i}" &
done
wait
```

## Container Management

### View Logs
```bash
# View container logs
docker logs helidon-mcp-server

# Follow logs in real-time
docker logs -f helidon-mcp-server

# View last 50 log lines
docker logs --tail 50 helidon-mcp-server
```

### Monitor Resources
```bash
# Monitor container resource usage
docker stats helidon-mcp-server

# Check container details
docker inspect helidon-mcp-server
```

### Container Lifecycle
```bash
# Stop the container
docker stop helidon-mcp-server

# Start the container
docker start helidon-mcp-server

# Restart the container
docker restart helidon-mcp-server

# Remove the container
docker rm -f helidon-mcp-server
```

## Troubleshooting

### Common Issues

| Issue | Solution |
|-------|----------|
| **Port 8080 already in use** | Use different port: `-p 8081:8080` |
| **Container exits immediately** | Check logs: `docker logs helidon-mcp-server` |
| **Connection refused** | Ensure container is running: `docker ps` |
| **Memory issues** | Increase memory: `-e JAVA_OPTS="-Xmx1g"` |

### Debug Commands
```bash
# Check container status
docker ps -a --filter name=helidon-mcp-server

# Check container logs for errors
docker logs helidon-mcp-server 2>&1 | grep -i error

# Test connectivity from inside container
docker exec helidon-mcp-server curl http://localhost:8080/

# Check Java process
docker exec helidon-mcp-server jps -v
```

## Development

### Building from Source
```bash
# Clone the repository
git clone https://github.com/thesurenk/helidon-mcp-server.git
cd helidon-mcp-server

# Build the Docker image
docker build -t surendocker/helidon-mcp-server .

# Run locally
docker run -d -p 8080:8080 surendocker/helidon-mcp-server
```

### Testing
```bash
# Run tests
mvn test

# Run with coverage
mvn test jacoco:report

# Run specific test suite
mvn test -Dtest=McpServerTest
```

## Image Details

- **Base Image**: Eclipse Temurin JRE 21 (slim)
- **Size**: Optimized for minimal footprint
- **Security**: Non-root user execution
- **Port**: 8080 (HTTP server)
- **Architecture**: Multi-platform support

## Contributing

This project is open for contributions! Feel free to:
- Report issues and bugs
- Suggest new features
- Submit pull requests
- Use as a template for your own MCP servers

## License

This project is provided as a base template for MCP server development. Feel free to modify and extend according to your needs.

## Resources

- [MCP Protocol Specification](https://modelcontextprotocol.io/)
- [Helidon Documentation](https://helidon.io/)
- [Docker Documentation](https://docs.docker.com/)
- [GitHub Repository](https://github.com/thesurenk/helidon-mcp-server)

---

**Created by Suren K** - A comprehensive MCP server implementation for modern Java applications.
