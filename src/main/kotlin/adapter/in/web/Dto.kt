package adapter.`in`.web

import domain.model.BuildingId
import domain.model.City
import domain.model.Lead
import domain.model.LeadId
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class LeadRequest(
    val name: String,
    val email: String,
    val phone: String,
    val buildingId: String,
    val city: String,
) {
    fun toDomain(): Lead = Lead(
        id = LeadId(UUID.randomUUID()),
        name = name,
        email = email,
        phone = phone,
        building = BuildingId(UUID.fromString(buildingId)),
        city = City(city),
    )
}

@Serializable
data class LeadResponse(val assignedAgentId: String)

@Serializable
data class ErrorResponse(val error: String)
