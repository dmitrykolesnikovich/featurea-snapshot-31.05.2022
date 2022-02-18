package featurea.ktor

import featurea.Properties
import featurea.log
import featurea.splitAndTrim
import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import kotlin.reflect.KProperty

typealias CommandBlock = suspend (message: String) -> Unit

typealias SocketProtocolBlock = SocketProtocol.() -> Unit

class SocketProtocol(val session: WebSocketSession, val userId: String) {

    internal val commandMap = mutableMapOf<String, CommandBlock>()
    val properties = Properties()

    fun COMMAND(command: String, block: CommandBlock) {
        commandMap[command] = block
    }

    class PropertyDelegate<T : Any>(val key: String, val defaultValue: () -> T) {

        inline operator fun <reified T : Any> getValue(socketProtocol: SocketProtocol, property: KProperty<*>): T {
            return socketProtocol.properties[key] ?: defaultValue() as T
        }

        inline operator fun <reified T : Any> setValue(protocol: SocketProtocol, property: KProperty<*>, value: T) {
            protocol.properties[key] = value
        }

    }

}

fun Application.installSocketProtocol(jwtService: JwtService, init: SocketProtocolBlock) {
    installWebSocketServer(jwtService) { userId, session ->
        val socketProtocol: SocketProtocol = SocketProtocol(session, userId)
        socketProtocol.init()
        try {
            log("[SocketProtocol.kt] installSocketProtocol: entering ($userId)")
            for (frame in session.incoming) {
                if (frame is Frame.Text) {
                    val text: String = frame.readText()
                    val (command, message) = text.splitAndTrim("\n", limit = 2)
                    val commandBlock = socketProtocol.commandMap[command] ?: continue
                    commandBlock(message)
                }
            }
            log("[SocketProtocol.kt] installSocketProtocol: complete ($userId)")
        } catch (e: Throwable) {
            log("[SocketProtocol.kt] [ERROR] installSocketProtocol: ${e.localizedMessage} ($userId)")
        }
    }
}

