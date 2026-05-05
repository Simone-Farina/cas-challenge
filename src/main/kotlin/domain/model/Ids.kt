package domain.model

import java.util.UUID

@JvmInline
value class AgentId(val value: UUID)

@JvmInline
value class BuildingId(val value: UUID)

@JvmInline
value class LeadId(val value: UUID)

@JvmInline
value class City(val value: String)
