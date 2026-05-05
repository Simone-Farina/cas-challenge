package application.service

import adapter.out.persistence.InMemoryAgentRepository
import application.port.`in`.RoutingResult
import domain.model.Agent
import domain.model.AgentId
import domain.model.BuildingId
import domain.model.City
import domain.model.Lead
import domain.model.LeadId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LeadRoutingServiceTest {

    private fun lead(city: String) = Lead(
        id = LeadId(UUID.randomUUID()),
        name = "Mario Rossi",
        email = "mario@example.com",
        phone = "+393454576234",
        building = BuildingId(UUID.randomUUID()),
        city = City(city),
    )

    @Test
    fun `assigns lead to least-loaded agent in matching city`() = runTest {
        val a = Agent(AgentId(UUID.randomUUID()), City("Milano"), assignedLeads = 3)
        val b = Agent(AgentId(UUID.randomUUID()), City("Milano"), assignedLeads = 1)
        val c = Agent(AgentId(UUID.randomUUID()), City("Roma"), assignedLeads = 0)

        val service = LeadRoutingService(
            InMemoryAgentRepository(mapOf(a.id to a, b.id to b, c.id to c))
        )

        val result = service.route(lead("Milano"))

        assertTrue(result is RoutingResult.Routed)
        assertEquals(b.id, result.agent.id)
        assertEquals(2, result.agent.assignedLeads)
        assertEquals(1, result.agent.version)
    }

    @Test
    fun `returns NoAgentAvailable when city has no agents`() = runTest {
        val agent = Agent(AgentId(UUID.randomUUID()), City("Roma"), assignedLeads = 1)
        val service = LeadRoutingService(InMemoryAgentRepository(mapOf(agent.id to agent)))

        val result = service.route(lead("Milano"))

        assertEquals(RoutingResult.NoAgentAvailable, result)
    }

    @Test
    fun `returns NoAgentAvailable when all agents in city are at capacity`() = runTest {
        val full = Agent(AgentId(UUID.randomUUID()), City("Milano"), assignedLeads = Agent.MAX_LEADS)
        val service = LeadRoutingService(InMemoryAgentRepository(mapOf(full.id to full)))

        val result = service.route(lead("Milano"))

        assertEquals(RoutingResult.NoAgentAvailable, result)
    }

    @Test
    fun `concurrent assignments never exceed total capacity`() = runBlocking {
        val agents = (1..4).map {
            Agent(AgentId(UUID.randomUUID()), City("Milano"), assignedLeads = 0)
        }
        val totalCapacity = agents.size * Agent.MAX_LEADS
        val repo = InMemoryAgentRepository(agents.associateBy { it.id })
        val service = LeadRoutingService(repo, maxRetries = 50)

        val attempts = totalCapacity + 100
        val results = (1..attempts).map {
            async(Dispatchers.Default) { service.route(lead("Milano")) }
        }.awaitAll()

        val routed = results.count { it is RoutingResult.Routed }

        assertEquals(totalCapacity, routed, "Must route exactly to total capacity, no over-assignment")
    }
}
