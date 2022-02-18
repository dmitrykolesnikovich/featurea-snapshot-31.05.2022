package featurea.ktor

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.routing.*
import io.ktor.sessions.*
import io.ktor.util.*
import java.text.DateFormat

@KtorExperimentalAPI
fun Application.installJsonRest(init: Route.() -> Unit) {
    install(ContentNegotiation) {
        gson {
            setDateFormat(DateFormat.LONG)
            setPrettyPrinting()
        }
    }
    routing {
        route("api") {
            init()
        }
    }
}
