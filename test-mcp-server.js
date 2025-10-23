// test-mcp-server.js - Test script for MCP server in E2B sandbox
import 'dotenv/config'
import { Sandbox } from '@e2b/code-interpreter'

async function testMcpServer() {
  console.log('🧪 Testing Helidon MCP Server in E2B Sandbox...')
  
  // Create sandbox with MCP server
  const sbx = await Sandbox.create({
    mcp: {
      'thesurenk/helidon-mcp-server': {
        installCmd: 'mvn clean package -DskipTests',
        runCmd: 'java -jar target/helidon-base-mcp-server-1.0.0.jar'
      }
    }
  })

  console.log('✅ Sandbox created:', sbx.id)
  console.log('🌐 MCP Server is running...')

  // Test 1: Basic HTTP connectivity
  console.log('\n📡 Test 1: Basic HTTP connectivity')
  try {
    const response = await sbx.runCode(`
import requests
import json

# Test root endpoint
response = requests.get('http://localhost:8080/')
print(f"Root endpoint: {response.status_code} - {response.text}")

# Test MCP endpoint
response = requests.get('http://localhost:8080/mcp')
print(f"MCP endpoint: {response.status_code} - {response.text}")
`)
    console.log('✅ HTTP tests completed')
  } catch (error) {
    console.log('❌ HTTP test failed:', error)
  }

  // Test 2: JSON-RPC Protocol
  console.log('\n📋 Test 2: JSON-RPC Protocol')
  try {
    const response = await sbx.runCode(`
import requests
import json

# Test JSON-RPC request
payload = {
    "jsonrpc": "2.0",
    "method": "mcp/list-tools",
    "id": 1
}

response = requests.post('http://localhost:8080/mcp', 
                        headers={'Content-Type': 'application/json'}, 
                        data=json.dumps(payload))

print(f"JSON-RPC response: {response.status_code}")
print(f"Response body: {response.text}")

# Test ping
payload = {
    "jsonrpc": "2.0",
    "method": "mcp/ping",
    "id": 2
}

response = requests.post('http://localhost:8080/mcp', 
                        headers={'Content-Type': 'application/json'}, 
                        data=json.dumps(payload))

print(f"Ping response: {response.status_code}")
print(f"Ping body: {response.text}")
`)
    console.log('✅ JSON-RPC tests completed')
  } catch (error) {
    console.log('❌ JSON-RPC test failed:', error)
  }

  // Test 3: Performance test
  console.log('\n⚡ Test 3: Performance test')
  try {
    const response = await sbx.runCode(`
import requests
import time
import concurrent.futures

def test_request():
    try:
        response = requests.get('http://localhost:8080/', timeout=5)
        return response.status_code == 200
    except:
        return False

# Test concurrent requests
start_time = time.time()
with concurrent.futures.ThreadPoolExecutor(max_workers=5) as executor:
    futures = [executor.submit(test_request) for _ in range(10)]
    results = [f.result() for f in futures]

end_time = time.time()
success_count = sum(results)

print(f"Concurrent requests: {success_count}/10 successful")
print(f"Time taken: {end_time - start_time:.2f} seconds")
`)
    console.log('✅ Performance test completed')
  } catch (error) {
    console.log('❌ Performance test failed:', error)
  }

  // Test 4: Check server logs
  console.log('\n📊 Test 4: Server status')
  try {
    const response = await sbx.runCode(`
import subprocess
import psutil

# Check if Java process is running
java_processes = []
for proc in psutil.process_iter(['pid', 'name', 'cmdline']):
    try:
        if 'java' in proc.info['name'].lower():
            java_processes.append(proc.info)
    except:
        pass

print(f"Java processes found: {len(java_processes)}")
for proc in java_processes:
    print(f"PID: {proc['pid']}, CMD: {' '.join(proc['cmdline'][:3])}")

# Check port 8080
import socket
sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
result = sock.connect_ex(('localhost', 8080))
sock.close()

if result == 0:
    print("✅ Port 8080 is open")
else:
    print("❌ Port 8080 is not accessible")
`)
    console.log('✅ Server status check completed')
  } catch (error) {
    console.log('❌ Server status check failed:', error)
  }

  console.log('\n🎉 All tests completed!')
  console.log('⏰ Keeping sandbox alive for 2 more minutes...')
  
  // Keep sandbox alive for 2 more minutes
  await new Promise(resolve => setTimeout(resolve, 120000))
  
  console.log('🛑 Closing sandbox...')
  await sbx.close()
  console.log('✅ Test session completed!')
}

testMcpServer().catch(console.error)
