// simple-test.js - Quick test for MCP server
import 'dotenv/config'
import { Sandbox } from '@e2b/code-interpreter'

async function quickTest() {
  console.log('🚀 Quick MCP Server Test...')
  
  const sbx = await Sandbox.create({
    mcp: {
      'thesurenk/helidon-mcp-server': {
        installCmd: 'mvn clean package -DskipTests',
        runCmd: 'java -jar target/helidon-base-mcp-server-1.0.0.jar'
      }
    }
  })

  console.log('✅ Sandbox created:', sbx.id)
  
  // Wait for server to start
  console.log('⏳ Waiting for server to start...')
  await new Promise(resolve => setTimeout(resolve, 10000)) // 10 seconds
  
  // Test the server
  console.log('🧪 Testing server...')
  const result = await sbx.runCode(`
import requests
import json

try:
    # Test root endpoint
    response = requests.get('http://localhost:8080/', timeout=5)
    print(f"✅ Root: {response.status_code} - {response.text}")
    
    # Test MCP endpoint
    response = requests.get('http://localhost:8080/mcp', timeout=5)
    print(f"✅ MCP: {response.status_code} - {response.text}")
    
    # Test JSON-RPC
    payload = {"jsonrpc":"2.0","method":"mcp/list-tools","id":1}
    response = requests.post('http://localhost:8080/mcp', 
                           headers={'Content-Type': 'application/json'}, 
                           data=json.dumps(payload), timeout=5)
    print(f"✅ JSON-RPC: {response.status_code} - {response.text}")
    
except Exception as e:
    print(f"❌ Error: {e}")
`)
  
  console.log('Test completed!')
  await sbx.close()
}

quickTest().catch(console.error)
