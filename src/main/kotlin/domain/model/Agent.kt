package domain.model

sealed interface AssignmentResult {
    data class Assigned(val agent: Agent) : AssignmentResult
    data object WrongCity : AssignmentResult
    data object AtCapacity : AssignmentResult
}

data class Agent(
    val id: AgentId,
    val city: City,
    val assignedLeads: Int = 0,
    val version: Int = 0,
) {
    val hasCapacity: Boolean get() = assignedLeads < MAX_LEADS

    fun assign(lead: Lead): AssignmentResult = when {
        lead.city != city -> AssignmentResult.WrongCity
        !hasCapacity -> AssignmentResult.AtCapacity
        else -> AssignmentResult.Assigned(
            copy(assignedLeads = assignedLeads + 1, version = version + 1)
        )
    }

    companion object {
        const val MAX_LEADS = 5
    }
}
