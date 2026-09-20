package areebah.nyuad4jetbrains.project.server

import areebah.nyuad4jetbrains.project.data.EventsResponse
import areebah.nyuad4jetbrains.project.data.curatedEvents
import areebah.nyuad4jetbrains.project.domain.Event
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.request.path
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

const val DEFAULT_PORT = 8080

@Serializable
data class Health(val status: String = "ok", val events: Int)

@Serializable
data class ApiError(val error: String)

/**
 * A small Ktor server for the curated community events.
 *
 * The point is that the curated list stops being baked into the app: one person can update events
 * here and every phone sees them, without a new build. It shares the app's own [Event] model
 * through the `shared` module, so the same Kotlin class defines both ends of the wire.
 *
 * Run with `./gradlew :server:run`, then `curl localhost:8080/events`.
 */
fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: DEFAULT_PORT
    embeddedServer(CIO, port = port, host = "0.0.0.0") {
        install(ContentNegotiation) { json(Json { prettyPrint = true }) }
        // The app runs on a phone and the preview in a desktop window, so no origin is meaningful.
        install(CORS) { anyHost() }

        routing {
            get("/health") { call.respond(Health(events = curatedEvents().size)) }

            get("/events") {
                val from = call.request.queryParameters["from"]?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
                val events = curatedEvents().filter { from == null || it.date >= from }.sortedBy { it.start }
                call.respond(EventsResponse(events))
            }

            get("/events/{id}") {
                val id = call.parameters["id"]
                val event = curatedEvents().firstOrNull { it.id == id }
                if (event == null) {
                    call.respond(io.ktor.http.HttpStatusCode.NotFound, ApiError("No event with id $id"))
                } else {
                    call.respond(event)
                }
            }
        }
    }.start(wait = true).also {
        println("Curated events API listening on http://localhost:$port (try /events)")
    }
}
