#!/bin/bash

# E2B.dev sandbox script for Helidon MCP Server
# This script helps run the MCP server in an E2B sandbox environment

set -e

echo "🚀 Running Helidon MCP Server in E2B Sandbox"
echo "============================================="

# Check if E2B SDK is installed
if ! npm list @e2b/code-interpreter &> /dev/null; then
    echo "📦 Installing E2B SDK..."
    npm install @e2b/code-interpreter dotenv
fi

# Check if .env file exists
if [ ! -f .env ]; then
    echo "❌ .env file not found. Creating from template..."
    if [ -f env.template ]; then
        cp env.template .env
        echo "✅ Created .env file from template"
        echo "📝 Please edit .env file and add your E2B API key:"
        echo "   E2B_API_KEY=e2b_***"
        echo "   Get your API key from: https://e2b.dev/dashboard"
        exit 1
    else
        echo "❌ env.template not found. Please create .env file manually:"
        echo "   E2B_API_KEY=e2b_***"
        exit 1
    fi
fi

echo "✅ E2B SDK ready"

# Create the MCP server runner script
cat > run-mcp-server.js << 'EOF'
import 'dotenv/config'
import { Sandbox } from '@e2b/code-interpreter'

console.log('🚀 Starting E2B Sandbox with Helidon MCP Server...')

// Create sandbox with custom MCP server from GitHub
const sbx = await Sandbox.create({
  mcp: {
    'thesurenk/helidon-mcp-server': {
      installCmd: 'mvn clean package -DskipTests',
      runCmd: 'java -jar target/helidon-base-mcp-server-1.0.0.jar'
    }
  }
})

console.log('✅ Sandbox created with MCP server:', sbx.id)
console.log('🌐 MCP Server is running and ready to accept connections')

// The MCP server is now running in the sandbox
// You can interact with it through the MCP protocol

// Keep the sandbox alive for 5 minutes
console.log('⏰ Sandbox will run for 5 minutes...')
await new Promise(resolve => setTimeout(resolve, 300000)) // 5 minutes

console.log('🛑 Closing sandbox...')
await sbx.close()
console.log('✅ Sandbox session completed!')
EOF

echo "🚀 Starting E2B MCP Server..."
node run-mcp-server.js

echo "✅ MCP Server session completed!"
