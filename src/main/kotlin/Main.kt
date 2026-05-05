import adapter.`in`.web.leadRoutes
import adapter.out.persistence.InMemoryAgentRepository
import application.service.LeadRoutingService
import domain.model.Agent
import domain.model.AgentId
import domain.model.City
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import java.util.UUID

fun main() {
    embeddedServer(Netty, port = 8080) { module() }.start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) { json() }

    val agents = InMemoryAgentRepository(seed = seedAgents())
    val routingService = LeadRoutingService(agents)

    routing {
        leadRoutes(routingService)
    }
}

private fun seedAgents(): Map<AgentId, Agent> {
    val milan = AgentId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
    val rome = AgentId(UUID.fromString("456e4567-e89b-12d3-a456-426614174000"))
    return mapOf(
        milan to Agent(id = milan, city = City("Milan"), assignedLeads = 0),
        rome to Agent(id = rome, city = City("Rome"), assignedLeads = 0),
    )
}
