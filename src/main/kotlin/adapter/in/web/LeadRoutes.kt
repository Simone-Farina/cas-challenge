package adapter.`in`.web

import application.port.`in`.RouteLeadUseCase
import application.port.`in`.RoutingResult
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

fun Route.leadRoutes(useCase: RouteLeadUseCase) {
    post("/v1/leads") {
        val lead = call.receive<LeadRequest>().toDomain()

        when (val result = useCase.route(lead)) {
            is RoutingResult.Routed -> call.respond(
                HttpStatusCode.Created,
                LeadResponse(result.agent.id.value.toString()),
            )
            RoutingResult.NoAgentAvailable -> call.respond(
                HttpStatusCode.UnprocessableEntity,
                ErrorResponse("No agents available in the city"),
            )
            RoutingResult.MaxRetriesExceeded -> call.respond(
                HttpStatusCode.ServiceUnavailable,
                ErrorResponse("System under heavy load. Please try again later."),
            )
        }
    }
}
