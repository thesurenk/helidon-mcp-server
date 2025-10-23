// E2B.dev sandbox runner for Helidon MCP Server
import 'dotenv/config'
import { Sandbox } from '@e2b/code-interpreter'

console.log('🚀 Starting E2B Sandbox for Helidon MCP Server...')

const sbx = await Sandbox.create({
  template: 'base',
  timeout: 300, // 5 minutes
  metadata: {
    name: 'helidon-mcp-server',
    description: 'MCP Server running in E2B sandbox'
  }
})

console.log('✅ Sandbox created:', sbx.id)

try {
  // Install Java and Maven
  console.log('📦 Installing Java and Maven...')
  await sbx.runCode(`
import subprocess
import os

# Install Java 21
subprocess.run(['apt-get', 'update'], check=True)
subprocess.run(['apt-get', 'install', '-y', 'openjdk-21-jdk'], check=True)

# Install Maven
subprocess.run(['apt-get', 'install', '-y', 'maven'], check=True)

# Verify installations
result = subprocess.run(['java', '-version'], capture_output=True, text=True)
print("Java version:", result.stdout)

result = subprocess.run(['mvn', '-version'], capture_output=True, text=True)
print("Maven version:", result.stdout)
`)

  // Clone and build the project
  console.log('📥 Cloning and building project...')
  await sbx.runCode(`
import subprocess
import os

# Clone the repository
subprocess.run(['git', 'clone', 'https://github.com/thesurenk/helidon-mcp-server.git'], check=True)
os.chdir('helidon-mcp-server')

# Build the project
subprocess.run(['mvn', 'clean', 'package', '-DskipTests'], check=True)
print("✅ Project built successfully")
`)

  // Start the server
  console.log('🚀 Starting MCP Server...')
  await sbx.runCode(`
import subprocess
import time
import threading
import requests
import signal
import sys

def start_server():
    # Start the server in background
    process = subprocess.Popen(['java', '-jar', 'target/helidon-base-mcp-server-1.0.0.jar'], 
                              stdout=subprocess.PIPE, 
                              stderr=subprocess.PIPE)
    return process

# Start server
server = start_server()
time.sleep(5)  # Wait for server to start

# Test the server
try:
    response = requests.get('http://localhost:8080/')
    print(f"Server response: {response.text}")
    print("✅ MCP Server is running!")
    
    # Test MCP endpoint
    response = requests.get('http://localhost:8080/mcp')
    print(f"MCP endpoint: {response.text}")
    
    # Test JSON-RPC
    import json
    payload = {"jsonrpc":"2.0","method":"mcp/list-tools","id":1}
    response = requests.post('http://localhost:8080/mcp', 
                           headers={'Content-Type': 'application/json'}, 
                           data=json.dumps(payload))
    print(f"JSON-RPC response: {response.text}")
    
except Exception as e:
    print(f"❌ Server test failed: {e}")

# Keep server running
print("🔄 Server is running... Will run for 4 minutes")
time.sleep(240)  # Run for 4 minutes

# Stop server
server.terminate()
print("🛑 Server stopped")
`)

  console.log('✅ MCP Server ran successfully in sandbox!')
  console.log('🌐 Server was available at: http://localhost:8080')
  console.log('📊 Sandbox ID:', sbx.id)

} catch (error) {
  console.error('❌ Error running sandbox:', error)
} finally {
  console.log('🛑 Closing sandbox...')
  await sbx.close()
  console.log('✅ Sandbox session completed!')
}
