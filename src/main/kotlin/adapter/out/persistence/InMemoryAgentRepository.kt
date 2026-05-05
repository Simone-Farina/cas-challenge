package adapter.out.persistence

import application.port.out.AgentRepository
import application.port.out.SaveResult
import domain.model.Agent
import domain.model.AgentId
import domain.model.City
import java.util.concurrent.ConcurrentHashMap

class InMemoryAgentRepository(seed: Map<AgentId, Agent> = emptyMap()) : AgentRepository {

    private val store: ConcurrentHashMap<AgentId, Agent> = ConcurrentHashMap(seed)

    override suspend fun findByCityWithCapacity(city: City): List<Agent> =
        store.values.filter { it.city == city && it.hasCapacity }

    override suspend fun save(agent: Agent): SaveResult {
        val current = store[agent.id]
            ?: return if (store.putIfAbsent(agent.id, agent) == null) {
                SaveResult.Saved(agent)
            } else {
                SaveResult.VersionConflict
            }

        if (current.version != agent.version - 1) {
            return SaveResult.VersionConflict
        }
        return if (store.replace(agent.id, current, agent)) {
            SaveResult.Saved(agent)
        } else {
            SaveResult.VersionConflict
        }
    }
}
