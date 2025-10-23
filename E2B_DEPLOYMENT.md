# E2B.dev Deployment Guide

This guide shows how to deploy the Helidon MCP Server to E2B.dev platform.

## Prerequisites

1. **E2B.dev Account**: Sign up at [e2b.dev](https://e2b.dev)
2. **E2B CLI**: Install the E2B command-line interface
3. **Docker Image**: Your image should be pushed to Docker Hub (already done: `surendocker/helidon-mcp-server`)

## Installation

### Install E2B CLI

```bash
# Install via npm
npm install -g @e2b/cli

# Or install via curl
curl -fsSL https://e2b.dev/install.sh | sh
```

### Login to E2B.dev

```bash
# Login to your E2B account
e2b auth login
```

## Deployment Methods

### Method 1: Using the Deployment Script (Recommended)

```bash
# Make the script executable
chmod +x deploy-e2b.sh

# Run the deployment script
./deploy-e2b.sh
```

### Method 2: Manual Deployment

```bash
# Create E2B project
e2b project create helidon-mcp-server --template docker

# Deploy using the configuration file
e2b deploy --config e2b.yaml
```

### Method 3: Direct CLI Deployment

```bash
# Deploy directly with CLI
e2b deploy \
  --name helidon-mcp-server \
  --image surendocker/helidon-mcp-server:latest \
  --port 8080 \
  --env JAVA_OPTS="-Xmx512m -Xms256m" \
  --env SERVER_PORT=8080
```

## Configuration

The `e2b.yaml` file contains the deployment configuration:

- **Image**: `surendocker/helidon-mcp-server:latest`
- **Port**: 8080 (HTTP server)
- **Memory**: 512Mi
- **CPU**: 0.5 cores
- **Environment**: Java optimization settings
- **Health Check**: Automatic health monitoring
- **Scaling**: Auto-scaling from 1 to 3 replicas

## Testing Your Deployment

### Basic Health Check

```bash
# Test the root endpoint
curl https://helidon-mcp-server.e2b.dev/

# Expected response: "Helidon MCP Server is running!"
```

### MCP Endpoint Test

```bash
# Test MCP endpoint
curl https://helidon-mcp-server.e2b.dev/mcp

# Expected response: "MCP endpoint available"
```

### JSON-RPC Protocol Test

```bash
# Test MCP protocol
curl -X POST https://helidon-mcp-server.e2b.dev/mcp \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","method":"mcp/list-tools","id":1}'

# Expected response: {"jsonrpc":"2.0","result":"MCP server response","id":1}
```

## Monitoring and Management

### View Logs

```bash
# View application logs
e2b logs helidon-mcp-server

# Follow logs in real-time
e2b logs -f helidon-mcp-server
```

### Check Status

```bash
# Check deployment status
e2b status helidon-mcp-server

# List all deployments
e2b list
```

### Scale Application

```bash
# Scale to 2 replicas
e2b scale helidon-mcp-server --replicas 2

# Auto-scale based on CPU
e2b autoscale helidon-mcp-server --cpu 70
```

## Environment Variables

You can customize the deployment with environment variables:

```bash
# Deploy with custom Java settings
e2b deploy \
  --name helidon-mcp-server \
  --image surendocker/helidon-mcp-server:latest \
  --env JAVA_OPTS="-Xmx1g -Xms512m" \
  --env LOG_LEVEL=DEBUG \
  --env SERVER_PORT=8080
```

## Custom Domain

To use a custom domain:

```bash
# Add custom domain
e2b domain add helidon-mcp-server your-domain.com

# Update DNS records as instructed
```

## SSL/TLS

E2B.dev automatically provides SSL certificates for your domain. Your application will be available at:

- **HTTPS**: `https://helidon-mcp-server.e2b.dev`
- **HTTP**: `http://helidon-mcp-server.e2b.dev` (redirects to HTTPS)

## Troubleshooting

### Common Issues

| Issue | Solution |
|-------|----------|
| **Deployment fails** | Check logs: `e2b logs helidon-mcp-server` |
| **Image not found** | Ensure image is pushed to Docker Hub |
| **Port conflicts** | Verify port 8080 is exposed in Dockerfile |
| **Memory issues** | Increase memory limit in e2b.yaml |

### Debug Commands

```bash
# Check deployment details
e2b describe helidon-mcp-server

# View resource usage
e2b metrics helidon-mcp-server

# Check health status
e2b health helidon-mcp-server
```

### Logs Analysis

```bash
# View recent logs
e2b logs --tail 100 helidon-mcp-server

# Filter error logs
e2b logs helidon-mcp-server | grep -i error

# View startup logs
e2b logs helidon-mcp-server | grep -i "started"
```

## Performance Optimization

### Resource Tuning

```yaml
# In e2b.yaml
resources:
  memory: 1Gi      # Increase for better performance
  cpu: 1.0         # Full CPU core
```

### Environment Optimization

```bash
# Deploy with optimized Java settings
e2b deploy \
  --env JAVA_OPTS="-Xmx1g -Xms512m -XX:+UseG1GC" \
  --env SERVER_PORT=8080
```

## Security

### Environment Variables

```bash
# Secure environment variables
e2b secret create helidon-mcp-server API_KEY=your-secret-key
e2b secret create helidon-mcp-server DB_PASSWORD=your-db-password
```

### Network Security

- E2B.dev provides automatic HTTPS
- DDoS protection included
- Firewall rules configurable
- Private networking available

## Cost Management

### Resource Limits

```yaml
# In e2b.yaml
resources:
  memory: 512Mi    # Start small
  cpu: 0.5         # Half CPU core
```

### Auto-scaling

```yaml
# In e2b.yaml
scaling:
  min_replicas: 1
  max_replicas: 3
  target_cpu: 70
```

## Updates and Rollbacks

### Update Deployment

```bash
# Update to new image version
e2b deploy --image surendocker/helidon-mcp-server:v2.0.0

# Rollback to previous version
e2b rollback helidon-mcp-server
```

### Blue-Green Deployment

```bash
# Deploy new version alongside old
e2b deploy --name helidon-mcp-server-v2 --image surendocker/helidon-mcp-server:v2.0.0

# Switch traffic to new version
e2b switch helidon-mcp-server helidon-mcp-server-v2
```

## Support

- **E2B Documentation**: https://e2b.dev/docs
- **Community Support**: https://discord.gg/e2b
- **GitHub Issues**: https://github.com/e2b-dev/e2b

---

**Created by Suren K** - Deploy your MCP server to the cloud with E2B.dev! 🚀
