#!/bin/bash

# E2B.dev deployment script for Helidon MCP Server
# This script helps deploy the MCP server to E2B.dev platform

set -e

echo "🚀 Deploying Helidon MCP Server to E2B.dev"
echo "=========================================="

# Check if E2B CLI is installed
if ! command -v e2b &> /dev/null; then
    echo "❌ E2B CLI not found. Please install it first:"
    echo "   npm install -g @e2b/cli"
    echo "   or visit: https://e2b.dev/docs"
    exit 1
fi

# Check if user is logged in
if ! e2b auth whoami &> /dev/null; then
    echo "🔐 Please login to E2B.dev first:"
    echo "   e2b auth login"
    exit 1
fi

echo "✅ E2B CLI found and authenticated"

# Create E2B project if it doesn't exist
echo "📦 Creating E2B project..."
e2b project create helidon-mcp-server --template docker || echo "Project already exists"

# Deploy the application
echo "🚀 Deploying application..."
e2b deploy --config e2b.yaml

echo "✅ Deployment completed!"
echo ""
echo "🌐 Your MCP server is now available at:"
echo "   https://helidon-mcp-server.e2b.dev"
echo ""
echo "🧪 Test your deployment:"
echo "   curl https://helidon-mcp-server.e2b.dev/"
echo "   curl https://helidon-mcp-server.e2b.dev/mcp"
echo ""
echo "📊 Monitor your deployment:"
echo "   e2b logs helidon-mcp-server"
echo "   e2b status helidon-mcp-server"
