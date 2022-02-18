package featurea.ktor

import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.time.Duration

fun Application.installWebSocketServer(jwtService: JwtService, block: JwtSessionBlock) {
    install(WebSockets) {
        pingPeriod = Duration.ofMinutes(1)
    }

    routing {
        webSocket("/wss") {
            val session: WebSocketServerSession = this
            try {
                val jwtToken: String = call.request.cookies["token"]!!
                val email: String = jwtService.findPayloadOrNullQuickfix(jwtToken, "email")!!
                block(email, session)
            } catch (e: Throwable) {
                e.printStackTrace()
                close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, e.localizedMessage))
            }
        }
    }
}

/*internals*/

private typealias JwtSessionBlock = suspend (String, WebSocketServerSession) -> Unit
