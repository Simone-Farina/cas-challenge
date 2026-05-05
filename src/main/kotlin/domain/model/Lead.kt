package domain.model

data class Lead(
    val id: LeadId,
    val name: String,
    val email: String,
    val phone: String,
    val building: BuildingId,
    val city: City,
)
