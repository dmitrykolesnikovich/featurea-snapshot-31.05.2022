package featurea.socket

import featurea.utils.ByteQueue

class SocketConnection(val config: SocketConnectionConfig) {

    val listeners = SocketConnectionDelegateListeners()
    init {
        listeners.config = config
    }
    private val delegate: SocketConnectionDelegate = SocketConnectionDelegate(listeners)

    fun isConnected(): Boolean = delegate.isConnected()
    fun start() = delegate.start()
    fun connect() = delegate.connect()
    fun disconnect() = delegate.disconnect()
    fun destroy() = delegate.destroy()
    fun write(bytes: ByteArray): Boolean = delegate.write(bytes)
}

expect class SocketConnectionConfig() {
    var path: String
    var ip: String
    var port: Int
    var retriesCount: Int
    var responseTimeout: Int
    var retryTimeout: Int
    var isTcpNoDelay: Boolean
}

expect open class SocketConnectionListener() {
    open fun onConnectSuccessSocketConnection()
    open fun onConnectFailedSocketConnection(e: Throwable)
    open fun onReconnectSuccessSocketConnection(e: Throwable)
    open fun onReconnectFailedSocketConnection(e: Throwable)
    open fun onDisconnectSocketConnection()
    open fun onThreadStartSocketConnection()
    open fun onThreadStopSocketConnection()
}

expect open class SocketConnectionResponseListener() {
    open fun onResponse(byteQueue: ByteQueue)
}

expect class SocketConnectionDelegateListeners() {
    var config: SocketConnectionConfig
    var responseListener: SocketConnectionResponseListener
    var connectionListener: SocketConnectionListener
}

expect class SocketConnectionDelegate(socketConnection: SocketConnectionDelegateListeners) {
    fun isConnected(): Boolean
    fun start()
    fun connect()
    fun disconnect()
    fun destroy()
    fun write(bytes: ByteArray): Boolean
}
