package application.port.`in`

import domain.model.Agent
import domain.model.Lead

sealed interface RoutingResult {
    data class Routed(val agent: Agent) : RoutingResult
    data object NoAgentAvailable : RoutingResult
    data object MaxRetriesExceeded : RoutingResult
}

fun interface RouteLeadUseCase {
    suspend fun route(lead: Lead): RoutingResult
}
