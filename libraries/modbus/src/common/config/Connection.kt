package featurea.modbus.config

import featurea.runtime.Module
import featurea.runtime.Component
import featurea.runtime.create
import featurea.script.Script
import featurea.socket.SocketConnectionConfig

class Connection(module: Module) : Directory(module) {

    var ip: String? = null
    var port: Int = -1
    var responseTimeout: Int = -1
    var retriesCount: Int = -1
    var retryTimeout: Int = -1
    var registerCount: Int = -1
    var hasRequest: Boolean = false
    var hasResponse: Boolean = false
    val channelByRegionMap = mutableMapOf<Region, LinkedHashSet<Channel>>()
    val channelCount: Int get() = channelByRegionMap.values.sumBy { it.size }

    override fun toString(): String = "Connection(name='$name')"

}

// constructor
fun Component.Connection(init: Connection.() -> Unit = {}): Connection = create(init)

// constructor
fun Directory.Connection(init: Connection.() -> Unit = {}): Connection = create(init).also { append(it) }

/*extensions*/

fun Connection.toSocketConnectionConfig(): SocketConnectionConfig? {
    val connection: Connection = this
    if (connection.ip.isNullOrBlank()) return null
    val connectionIp: String = connection.ip ?: return null
    return SocketConnectionConfig().apply {
        path = connection.path
        ip = connectionIp
        port = connection.port
        retriesCount = connection.retriesCount
        responseTimeout = connection.responseTimeout
        retryTimeout = connection.retryTimeout
    }
}
