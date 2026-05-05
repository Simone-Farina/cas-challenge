package application.service

import application.port.`in`.RouteLeadUseCase
import application.port.`in`.RoutingResult
import application.port.out.AgentRepository
import application.port.out.SaveResult
import domain.model.AssignmentResult
import domain.model.Lead

class LeadRoutingService(
    private val agents: AgentRepository,
    private val maxRetries: Int = DEFAULT_MAX_RETRIES,
) : RouteLeadUseCase {

    override suspend fun route(lead: Lead): RoutingResult {
        repeat(maxRetries) {
            val candidate = agents.findByCityWithCapacity(lead.city)
                .minByOrNull { it.assignedLeads }
                ?: return RoutingResult.NoAgentAvailable

            val assignment = candidate.assign(lead)
            if (assignment !is AssignmentResult.Assigned) {
                return@repeat
            }

            when (agents.save(assignment.agent)) {
                is SaveResult.Saved -> return RoutingResult.Routed(assignment.agent)
                SaveResult.VersionConflict -> return@repeat
            }
        }
        return RoutingResult.MaxRetriesExceeded
    }

    companion object {
        const val DEFAULT_MAX_RETRIES = 3
    }
}
