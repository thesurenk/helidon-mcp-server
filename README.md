# Helidon Base MCP Server

A base MCP (Model Context Protocol) server implementation using Helidon framework, containerized with Docker for easy deployment and testing.

## Overview

This project provides a foundation for building MCP servers using Java and Helidon. It includes a simple ping tool as an example and demonstrates how to implement the MCP protocol for tool discovery and execution.

## Features

- **MCP Protocol Support**: Implements the Model Context Protocol for tool discovery and execution
- **Docker Containerized**: Ready-to-use Docker image with multi-stage build
- **Java 21**: Built with modern Java features and Helidon 4.0.7
- **Self-contained JAR**: All dependencies included in a single executable JAR
- **HTTP Server**: RESTful API endpoints for MCP protocol communication
- **JSON-RPC Support**: Full JSON-RPC 2.0 protocol implementation

## Project Structure

```
helidon-base-mcp-server/
├── src/
│   └── main/
│       └── java/
│           └── io/
│               └── helidon/
│                   └── McpServer.java    # Main MCP server implementation
├── pom.xml                              # Maven configuration
├── Dockerfile                           # Multi-stage Docker build
└── README.md                            # This file
```

## Prerequisites

- **Java 21** or higher
- **Maven 3.6+** (for local development)
- **Docker** (for containerized deployment)

## 🚀 Complete Docker Deployment & Testing Guide

### Prerequisites Checklist

Before starting, ensure you have:
- [ ] **Docker Desktop** installed and running
- [ ] **PowerShell** (Windows) or **Bash** (Linux/Mac) terminal
- [ ] **Internet connection** for downloading base images
- [ ] **Port 8080** available on your system

### Step 1: Build the Docker Image

```bash
# Navigate to project directory
cd helidon-base-mcp-server

# Build the Docker image with verbose output
docker build -t helidon-base-mcp . --progress=plain

# Verify image was created
docker images | grep helidon-base-mcp
```

**Expected Output:**
```
REPOSITORY          TAG       IMAGE ID       CREATED         SIZE
helidon-base-mcp   latest    abc123def456   2 minutes ago   200MB
```

### Step 2: Deploy the Container

```bash
# Run the container in detached mode with proper naming
docker run -d \
  --name helidon-mcp-server \
  -p 8080:8080 \
  --restart unless-stopped \
  helidon-base-mcp

# Verify container is running
docker ps --filter name=helidon-mcp-server
```

**Expected Output:**
```
CONTAINER ID   IMAGE              COMMAND               CREATED         STATUS         PORTS                    NAMES
134e7fda648f   helidon-base-mcp   "java -jar app.jar"   3 minutes ago   Up 3 minutes   0.0.0.0:8080->8080/tcp   helidon-mcp-server
```

### Step 3: Health Check & Monitoring

```bash
# Check container health
docker ps --filter name=helidon-mcp-server

# View container logs
docker logs helidon-mcp-server

# Monitor logs in real-time
docker logs -f helidon-mcp-server
```

**Expected Log Output:**
```
Oct 23, 2025 4:47:48 PM io.helidon.common.features.HelidonFeatures features
INFO: Helidon SE 4.0.7 features: [WebServer]
Oct 23, 2025 4:47:48 PM io.helidon.webserver.ServerListener start
INFO: [0x77aaa2fc] http://0.0.0.0:8080 bound for socket '@default'
Oct 23, 2025 4:47:48 PM io.helidon.webserver.LoomServer startIt
INFO: Started all channels in 41 milliseconds. 273 milliseconds since JVM startup. Java 21.0.8+9-LTS
```

### Step 4: Comprehensive Testing Suite

#### 4.1 Basic Connectivity Tests

```bash
# Test 1: Basic HTTP connectivity
curl -v http://localhost:8080/

# Test 2: MCP endpoint availability
curl -v http://localhost:8080/mcp

# Test 3: Server response time
time curl -s http://localhost:8080/ > /dev/null
```

**Expected Responses:**
- `GET /` → `"Helidon MCP Server is running!"`
- `GET /mcp` → `"MCP endpoint available"`

#### 4.2 JSON-RPC Protocol Testing

**PowerShell (Windows):**
```powershell
# Test MCP list-tools
$body = '{"jsonrpc":"2.0","method":"mcp/list-tools","id":1}'
$response = Invoke-WebRequest -Uri "http://localhost:8080/mcp" -Method POST -ContentType "application/json" -Body $body
Write-Host "Status: $($response.StatusCode)"
Write-Host "Response: $($response.Content)"

# Test MCP ping
$pingBody = '{"jsonrpc":"2.0","method":"mcp/ping","id":2}'
$pingResponse = Invoke-WebRequest -Uri "http://localhost:8080/mcp" -Method POST -ContentType "application/json" -Body $pingBody
Write-Host "Ping Response: $($pingResponse.Content)"
```

**Bash/cURL (Linux/Mac):**
```bash
# Test MCP list-tools
curl -X POST http://localhost:8080/mcp \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","method":"mcp/list-tools","id":1}' \
  -w "\nHTTP Status: %{http_code}\nResponse Time: %{time_total}s\n"

# Test MCP ping
curl -X POST http://localhost:8080/mcp \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","method":"mcp/ping","id":2}' \
  -w "\nHTTP Status: %{http_code}\nResponse Time: %{time_total}s\n"
```

**Expected JSON-RPC Responses:**
```json
{"jsonrpc":"2.0","result":"MCP server response","id":1}
```

#### 4.3 Performance & Load Testing

```bash
# Concurrent request testing
for i in {1..10}; do
  curl -X POST http://localhost:8080/mcp \
    -H "Content-Type: application/json" \
    -d "{\"jsonrpc\":\"2.0\",\"method\":\"mcp/ping\",\"id\":$i}" &
done
wait

# Response time analysis
echo "Testing response times..."
for i in {1..5}; do
  echo "Test $i:"
  time curl -s -X POST http://localhost:8080/mcp \
    -H "Content-Type: application/json" \
    -d '{"jsonrpc":"2.0","method":"mcp/list-tools","id":1}' > /dev/null
done
```

#### 4.4 Error Handling Tests

```bash
# Test invalid JSON
curl -X POST http://localhost:8080/mcp \
  -H "Content-Type: application/json" \
  -d '{"invalid":"json"}' \
  -v

# Test malformed JSON-RPC
curl -X POST http://localhost:8080/mcp \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","method":"unknown-method","id":999}' \
  -v

# Test wrong content type
curl -X POST http://localhost:8080/mcp \
  -H "Content-Type: text/plain" \
  -d "plain text" \
  -v
```

### Step 5: Container Management

#### 5.1 Container Lifecycle Management

```bash
# Stop the container
docker stop helidon-mcp-server

# Start the container
docker start helidon-mcp-server

# Restart the container
docker restart helidon-mcp-server

# Remove the container (stops it first)
docker rm -f helidon-mcp-server
```

#### 5.2 Resource Monitoring

```bash
# Monitor container resource usage
docker stats helidon-mcp-server

# Check container details
docker inspect helidon-mcp-server

# View container processes
docker exec helidon-mcp-server ps aux
```

#### 5.3 Log Management

```bash
# View last 50 log lines
docker logs --tail 50 helidon-mcp-server

# View logs with timestamps
docker logs -t helidon-mcp-server

# Save logs to file
docker logs helidon-mcp-server > server-logs.txt

# Follow logs in real-time
docker logs -f --tail 10 helidon-mcp-server
```

### Step 6: Production Deployment Considerations

#### 6.1 Environment Configuration

```bash
# Run with custom environment variables
docker run -d \
  --name helidon-mcp-server \
  -p 8080:8080 \
  -e JAVA_OPTS="-Xmx512m -Xms256m" \
  -e SERVER_PORT=8080 \
  --restart unless-stopped \
  helidon-base-mcp
```

#### 6.2 Network Configuration

```bash
# Create custom network
docker network create mcp-network

# Run container on custom network
docker run -d \
  --name helidon-mcp-server \
  --network mcp-network \
  -p 8080:8080 \
  helidon-base-mcp
```

#### 6.3 Volume Mounting (for persistent data)

```bash
# Run with volume for logs
docker run -d \
  --name helidon-mcp-server \
  -p 8080:8080 \
  -v $(pwd)/logs:/app/logs \
  helidon-base-mcp
```

### Step 7: Troubleshooting & Debugging

#### 7.1 Common Issues & Solutions

| Issue | Symptoms | Solution |
|-------|----------|----------|
| **Port Conflict** | `bind: address already in use` | Change port: `-p 8081:8080` |
| **Memory Issues** | Container exits immediately | Increase memory: `-e JAVA_OPTS="-Xmx1g"` |
| **Network Issues** | Connection refused | Check firewall and port forwarding |
| **Image Not Found** | `Unable to find image` | Rebuild: `docker build -t helidon-base-mcp .` |

#### 7.2 Debug Commands

```bash
# Check container status
docker ps -a --filter name=helidon-mcp-server

# Inspect container configuration
docker inspect helidon-mcp-server

# Check container logs for errors
docker logs helidon-mcp-server 2>&1 | grep -i error

# Test connectivity from inside container
docker exec helidon-mcp-server curl http://localhost:8080/

# Check Java process
docker exec helidon-mcp-server jps -v
```

### Step 8: Cleanup & Maintenance

```bash
# Stop and remove container
docker stop helidon-mcp-server
docker rm helidon-mcp-server

# Remove image (if needed)
docker rmi helidon-base-mcp

# Clean up unused resources
docker system prune -f

# Remove all stopped containers
docker container prune -f
```

### Step 9: Automated Testing Script

Create a comprehensive test script:

```bash
#!/bin/bash
# test-mcp-server.sh

echo "🚀 Starting Helidon MCP Server Testing Suite"
echo "=============================================="

# Test 1: Basic connectivity
echo "📡 Testing basic connectivity..."
if curl -s http://localhost:8080/ > /dev/null; then
    echo "✅ Basic connectivity: PASS"
else
    echo "❌ Basic connectivity: FAIL"
    exit 1
fi

# Test 2: MCP endpoint
echo "🔧 Testing MCP endpoint..."
if curl -s http://localhost:8080/mcp > /dev/null; then
    echo "✅ MCP endpoint: PASS"
else
    echo "❌ MCP endpoint: FAIL"
    exit 1
fi

# Test 3: JSON-RPC protocol
echo "📋 Testing JSON-RPC protocol..."
response=$(curl -s -X POST http://localhost:8080/mcp \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","method":"mcp/list-tools","id":1}')

if echo "$response" | grep -q "jsonrpc"; then
    echo "✅ JSON-RPC protocol: PASS"
    echo "   Response: $response"
else
    echo "❌ JSON-RPC protocol: FAIL"
    echo "   Response: $response"
    exit 1
fi

echo "🎉 All tests passed! MCP server is working correctly."
```

**Usage:**
```bash
chmod +x test-mcp-server.sh
./test-mcp-server.sh
```

### Quick Start

For a quick start, use these essential commands:

```bash
# Build and run
docker build -t helidon-base-mcp .
docker run -d -p 8080:8080 --name helidon-mcp-server helidon-base-mcp

# Test
curl http://localhost:8080/
curl -X POST http://localhost:8080/mcp -H "Content-Type: application/json" -d '{"jsonrpc":"2.0","method":"mcp/list-tools","id":1}'
```

### Local Development

1. **Build the project:**
   ```bash
   mvn clean package
   ```

2. **Run the server:**
   ```bash
   java -jar target/helidon-base-mcp-server-1.0.0.jar
   ```

3. **Test the server:**
   ```bash
   # Test basic endpoint
   curl http://localhost:8080/
   
   # Test MCP endpoint
   curl http://localhost:8080/mcp
   
   # Test with MCP client (PowerShell)
   Invoke-WebRequest -Uri "http://localhost:8080/mcp" -Method POST -ContentType "application/json" -Body '{"jsonrpc":"2.0","method":"mcp/list-tools","id":1}'
   
   # Test with MCP client (curl)
   curl -X POST http://localhost:8080/mcp \
     -H "Content-Type: application/json" \
     -d '{"jsonrpc":"2.0","method":"mcp/list-tools","id":1}'
   ```

## MCP Protocol Implementation

The server implements a basic MCP protocol with HTTP endpoints:

### Available Endpoints
- **GET `/`**: Returns "Helidon MCP Server is running!"
- **GET `/mcp`**: Returns "MCP endpoint available"
- **POST `/mcp`**: Handles JSON-RPC requests

### JSON-RPC Support
The server responds to JSON-RPC requests with:
```json
{"jsonrpc":"2.0","result":"MCP server response","id":1}
```

### Example Requests
- **Tool Discovery**: `{"jsonrpc":"2.0","method":"mcp/list-tools","id":1}`
- **Ping**: `{"jsonrpc":"2.0","method":"mcp/ping","id":2}`

## Docker Details

### Multi-stage Build
The Dockerfile uses a multi-stage build process:

1. **Build Stage**: Uses `maven:3.9.6-eclipse-temurin-21` to compile and package the application
2. **Runtime Stage**: Uses `eclipse-temurin:21-jre` for a smaller final image

### Image Optimization
- **Base Image**: Eclipse Temurin JRE 21 (slim)
- **Final Size**: Optimized for minimal footprint
- **Security**: Non-root user execution
- **Port**: Exposes port 8080 for HTTP endpoints

## Testing

The project includes a comprehensive test suite with unit tests, integration tests, and performance tests to ensure the MCP server works correctly.

### Test Suite Overview

The test suite includes:

- **Unit Tests** (`McpServerTest.java`): Test individual components and endpoints
- **Integration Tests** (`McpServerIntegrationTest.java`): Test full system behavior with Testcontainers
- **Performance Tests** (`McpServerPerformanceTest.java`): Test server performance and load handling
- **Test Utilities** (`TestUtils.java`): Helper classes for testing MCP protocol functionality

### Running Tests

#### Prerequisites for Testing

- **Java 21** or higher
- **Maven 3.6+**
- **Docker** (for integration tests with Testcontainers)

#### Running All Tests

```bash
# Run all tests
mvn test

# Run tests with coverage report
mvn test jacoco:report

# Run tests with verbose output
mvn test -X
```

#### Running Specific Test Suites

```bash
# Run only unit tests
mvn test -Dtest=McpServerTest

# Run only integration tests
mvn test -Dtest=McpServerIntegrationTest

# Run only performance tests
mvn test -Dtest=McpServerPerformanceTest

# Run tests matching a pattern
mvn test -Dtest="*Test"
```

#### Test Categories

**Unit Tests** - Fast, isolated tests:
- Server startup and shutdown
- HTTP endpoint functionality
- JSON-RPC response validation
- Error handling (404s, malformed requests)
- Port configuration

**Integration Tests** - Full system tests:
- Concurrent request handling
- MCP protocol compliance
- Large payload handling
- Malformed JSON handling
- Server state persistence

**Performance Tests** - Load and stress tests:
- High throughput testing (1000+ requests)
- Sustained load testing
- Memory efficiency testing
- Response time analysis

### Test Configuration

The tests are configured in `pom.xml` with:

- **JUnit Jupiter 5.10.1** for unit testing
- **Testcontainers 1.19.3** for integration testing
- **REST Assured 5.3.2** for HTTP testing
- **JaCoCo 0.8.11** for code coverage
- **Maven Surefire Plugin** for unit tests
- **Maven Failsafe Plugin** for integration tests

### Test Examples

#### Unit Test Example

```java
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
}
```

#### Integration Test Example

```java
@Test
@DisplayName("Should handle concurrent requests")
void shouldHandleConcurrentRequests() throws Exception {
    startServer(0);
    int port = server.port();
    
    // Create multiple concurrent requests
    int numberOfRequests = 10;
    // ... test implementation
}
```

#### Performance Test Example

```java
@Test
@DisplayName("Should handle high throughput requests")
@Disabled("Performance test - run manually when needed")
void shouldHandleHighThroughputRequests() throws Exception {
    // Test with 1000+ concurrent requests
    // Measure response times and success rates
}
```

### Test Utilities

The `TestUtils` class provides helper methods for:

- **HTTP Request Building**: `createGetRequest()`, `createPostRequest()`
- **MCP Protocol Helpers**: `createMcpInitRequest()`, `createMcpRequest()`
- **Concurrent Testing**: `sendConcurrentRequests()`
- **Server Readiness**: `waitForServer()`
- **JSON-RPC Validation**: `isValidJsonRpcResponse()`

### Continuous Integration

The test suite is designed to work with CI/CD pipelines:

```yaml
# Example GitHub Actions workflow
name: Test Suite
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '21'
      - name: Run tests
        run: mvn test
      - name: Generate coverage report
        run: mvn jacoco:report
```

### Manual Testing

You can also test the server manually using the following methods:

1. **Basic HTTP Testing**:
   ```bash
   # Test server is running
   curl http://localhost:8080/
   
   # Test MCP endpoint
   curl http://localhost:8080/mcp
   ```

2. **JSON-RPC Testing**:
   ```bash
   # PowerShell
   Invoke-WebRequest -Uri "http://localhost:8080/mcp" -Method POST -ContentType "application/json" -Body '{"jsonrpc":"2.0","method":"mcp/list-tools","id":1}'
   
   # curl
   curl -X POST http://localhost:8080/mcp \
     -H "Content-Type: application/json" \
     -d '{"jsonrpc":"2.0","method":"mcp/list-tools","id":1}'
   ```

3. **Docker Container Testing**:
   ```bash
   # Check container status
   docker ps
   
   # View container logs
   docker logs helidon-mcp-server
   
   # Test container endpoints
   docker exec helidon-mcp-server curl http://localhost:8080/
   ```

### Test Requirements

- **Docker**: Required for containerized testing
- **Network**: Internet access for downloading Docker images
- **PowerShell**: For Windows testing commands

### Test Troubleshooting

#### Common Test Issues

| Issue | Symptoms | Solution |
|-------|----------|----------|
| **Testcontainers fails** | `Could not find a valid Docker environment` | Ensure Docker Desktop is running |
| **Port conflicts** | `Address already in use` | Use random ports in tests (`startServer(0)`) |
| **Memory issues** | `OutOfMemoryError` | Increase Maven heap: `export MAVEN_OPTS="-Xmx2g"` |
| **Timeout errors** | `TimeoutException` | Increase test timeouts in `@Test` methods |

#### Test Debug Commands

```bash
# Run tests with debug output
mvn test -X

# Run specific test with debug
mvn test -Dtest=McpServerTest -X

# Run tests with increased memory
export MAVEN_OPTS="-Xmx2g -XX:+UseG1GC"
mvn test

# Check test dependencies
mvn dependency:tree

# Clean and rebuild before testing
mvn clean test
```

#### Test Best Practices

1. **Isolation**: Each test should be independent and not rely on other tests
2. **Cleanup**: Always clean up resources in `@AfterEach` methods
3. **Random Ports**: Use `startServer(0)` to avoid port conflicts
4. **Timeouts**: Set appropriate timeouts for HTTP requests
5. **Assertions**: Use descriptive assertion messages
6. **Performance**: Mark performance tests with `@Disabled` for regular runs

#### Test Coverage

Generate and view test coverage reports:

```bash
# Generate coverage report
mvn test jacoco:report

# View coverage report
open target/site/jacoco/index.html

# Check coverage threshold
mvn jacoco:check
```

#### Test Environment Setup

For consistent testing across environments:

```bash
# Set up test environment variables
export TEST_CONTAINERS_RYUK_DISABLED=true
export DOCKER_HOST=tcp://localhost:2375

# Run tests in parallel (faster)
mvn test -T 4

# Run tests with specific profile
mvn test -P integration-tests
```

## Development

### Adding New Tools

To add a new tool to the MCP server, modify the `McpServer.java` file:

1. **Add new endpoint** in the routing configuration:
   ```java
   .get("/your-endpoint", (req, res) -> res.send("Your tool response"))
   .post("/your-endpoint", (req, res) -> {
       // Handle JSON-RPC requests for your tool
       res.headers().add(HeaderNames.CONTENT_TYPE, "application/json");
       res.send("{\"jsonrpc\":\"2.0\",\"result\":\"Your tool result\",\"id\":1}");
   })
   ```

2. **Implement tool logic** in the POST handler:
   ```java
   .post("/your-endpoint", (req, res) -> {
       // Parse JSON-RPC request
       // Implement your tool logic
       // Return JSON-RPC response
       res.headers().add(HeaderNames.CONTENT_TYPE, "application/json");
       res.send("{\"jsonrpc\":\"2.0\",\"result\":\"Your tool result\",\"id\":1}");
   })
   ```

3. **Test your tool**:
   ```bash
   # Test GET endpoint
   curl http://localhost:8080/your-endpoint
   
   # Test POST endpoint with JSON-RPC
   curl -X POST http://localhost:8080/your-endpoint \
     -H "Content-Type: application/json" \
     -d '{"jsonrpc":"2.0","method":"your-method","id":1}'
   ```

### Building and Testing

```bash
# Clean and compile
mvn clean compile

# Package application
mvn package

# Build Docker image
docker build -t helidon-base-mcp .

# Test Docker image
docker run -d -p 8080:8080 --name helidon-mcp-server helidon-base-mcp

# Test the server
curl http://localhost:8080/
curl http://localhost:8080/mcp

# Test JSON-RPC (PowerShell)
Invoke-WebRequest -Uri "http://localhost:8080/mcp" -Method POST -ContentType "application/json" -Body '{"jsonrpc":"2.0","method":"mcp/list-tools","id":1}'

# Test JSON-RPC (curl)
curl -X POST http://localhost:8080/mcp \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","method":"mcp/list-tools","id":1}'
```

## Configuration

### Maven Configuration
The project uses Maven with the following key configurations:

- **Java Version**: 21
- **Helidon Version**: 4.0.7
- **Packaging**: JAR with shade plugin for fat JAR
- **Dependencies**: Helidon WebServer for HTTP server functionality
- **Main Class**: `io.helidon.McpServer`

### Docker Configuration
- **Base Images**: Maven 3.9.6 + Eclipse Temurin 21
- **Working Directory**: `/app`
- **JAR Location**: `app.jar`
- **Entry Point**: `java -jar app.jar`
- **Port**: 8080 (HTTP server)

## Troubleshooting

### Common Issues

1. **ClassNotFoundException**: Ensure the main class name in `pom.xml` matches the actual class name
2. **Docker Build Fails**: Check that Docker Desktop is running and has sufficient resources
3. **Java Version Issues**: Ensure using Java 21 for Helidon 4.0.7 compatibility
4. **MCP Protocol Issues**: Verify JSON-RPC format and method names
5. **Port Conflicts**: Ensure port 8080 is not already in use

### Debug Mode

Run with debug output:
```bash
# Local
java -Ddebug=true -jar target/helidon-base-mcp-server-1.0.0.jar

# Docker
docker run -e DEBUG=true -p 8080:8080 --name helidon-mcp-server helidon-base-mcp

# Check container logs
docker logs helidon-mcp-server

# Check container status
docker ps
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test with Docker
5. Submit a pull request

## About the Creator

**Suren K** - Creator and Maintainer

This MCP server project was created by Suren K as a comprehensive foundation for building Model Context Protocol servers using Java and Helidon. The project demonstrates best practices in:

- **Modern Java Development**: Using Java 21 with Helidon 4.0.7 framework
- **Containerization**: Docker-based deployment with multi-stage builds
- **Testing Excellence**: Comprehensive test suite with unit, integration, and performance tests
- **Documentation**: Detailed README with deployment guides and troubleshooting
- **Code Quality**: Proper JavaDoc documentation and author attribution

The project serves as both a working MCP server implementation and an educational resource for developers looking to understand MCP protocol implementation, modern Java web services, and containerized application development.

### Contact & Contributions

This project is open for contributions and improvements. Feel free to:
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
- [Maven Documentation](https://maven.apache.org/)
