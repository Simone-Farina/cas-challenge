package application.port.out

import domain.model.Agent
import domain.model.City

sealed interface SaveResult {
    data class Saved(val agent: Agent) : SaveResult
    data object VersionConflict : SaveResult
}

interface AgentRepository {
    suspend fun findByCityWithCapacity(city: City): List<Agent>
    suspend fun save(agent: Agent): SaveResult
}
