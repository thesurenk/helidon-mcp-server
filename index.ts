// index.ts - E2B MCP Server Runner
import 'dotenv/config'
import { Sandbox } from '@e2b/code-interpreter'

async function main() {
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
}

main().catch(console.error)