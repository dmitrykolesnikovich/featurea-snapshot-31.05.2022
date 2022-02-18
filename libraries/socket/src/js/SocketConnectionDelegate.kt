package featurea.socket

import featurea.ByteQueue

actual class SocketConnectionConfig {
    actual var path: String = error("path")
    actual var ip: String = error("ip")
    actual var port: Int = error("port")
    actual var retriesCount: Int = error("retriesCount")
    actual var responseTimeout: Int = error("responseTimeout")
    actual var retryTimeout: Int = error("retryTimeout")
    actual var isTcpNoDelay: Boolean = error("isTcpNoDelay")
}

actual open class SocketConnectionListener {
    actual open fun onConnectSuccessSocketConnection(): Unit = error("listener")
    actual open fun onConnectFailedSocketConnection(e: Throwable): Unit = error("")
    actual open fun onReconnectSuccessSocketConnection(e: Throwable): Unit = error("listener")
    actual open fun onReconnectFailedSocketConnection(e: Throwable): Unit = error("listener")
    actual open fun onDisconnectSocketConnection(): Unit = error("listener")
    actual open fun onThreadStartSocketConnection(): Unit = error("listener")
    actual open fun onThreadStopSocketConnection(): Unit = error("listener")
}

actual open class SocketConnectionResponseListener {
    actual open fun onResponse(byteQueue: ByteQueue): Unit = error("listener")
}

actual class SocketConnectionDelegateListeners {
    actual var config: SocketConnectionConfig = error("config")
    actual var responseListener: SocketConnectionResponseListener = error("listener")
    actual var connectionListener: SocketConnectionListener = error("listener")
}

actual class SocketConnectionDelegate actual constructor(val socketConnection: SocketConnectionDelegateListeners) {
    actual fun isConnected(): Boolean = error("isConnected")
    actual fun start(): Unit = error("start")
    actual fun connect(): Unit = error("connect")
    actual fun disconnect(): Unit = error("disconnect")
    actual fun destroy(): Unit = error("destroy")
    actual fun write(bytes: ByteArray): Boolean = error("write")
}
