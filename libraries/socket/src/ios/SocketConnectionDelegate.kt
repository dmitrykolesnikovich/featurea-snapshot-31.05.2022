package featurea.socket

import featurea.utils.log
import featurea.utils.ByteQueue
import featurea.ios.socket.SocketConnectionProtocol
import featurea.ios.socket.SocketConnectionService
import featurea.ios.toException
import featurea.ios.toNSData
import featurea.utils.toByteArray
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSNumber
import platform.darwin.NSObject

actual class SocketConnectionConfig {
    actual var path: String = ""
    actual var ip: String = ""
    actual var port: Int = -1
    actual var retriesCount: Int = -1
    actual var responseTimeout: Int = -1
    actual var retryTimeout: Int = -1
    actual var isTcpNoDelay: Boolean = true // https://stackoverflow.com/a/34780678/909169
}

actual open class SocketConnectionListener {
    actual open fun onConnectSuccessSocketConnection() {}
    actual open fun onConnectFailedSocketConnection(e: Throwable) {}
    actual open fun onReconnectSuccessSocketConnection(e: Throwable) {}
    actual open fun onReconnectFailedSocketConnection(e: Throwable) {}
    actual open fun onDisconnectSocketConnection() {}
    actual open fun onThreadStartSocketConnection() {}
    actual open fun onThreadStopSocketConnection() {}
}

actual open class SocketConnectionResponseListener {
    actual open fun onResponse(byteQueue: ByteQueue) {}
}

actual class SocketConnectionDelegateListeners {
    actual var config: SocketConnectionConfig = SocketConnectionConfig()
    actual var responseListener: SocketConnectionResponseListener = SocketConnectionResponseListener()
    actual var connectionListener: SocketConnectionListener = SocketConnectionListener()
}

@OptIn(ExperimentalUnsignedTypes::class)
actual class SocketConnectionDelegate actual constructor(private val socketConnection: SocketConnectionDelegateListeners) {

    private val socketConnectionProtocol = object : SocketConnectionProtocol, NSObject() {

        private val dataBuffer = ByteQueue()

        override fun onConnectFailedSocketConnectionWithError(error: NSError) {
            initRuntimeIfNeeded()
            socketConnection.connectionListener.onConnectFailedSocketConnection(error.toException())
        }

        override fun onConnectSuccessSocketConnection() {
            initRuntimeIfNeeded()
            socketConnection.connectionListener.onConnectSuccessSocketConnection()
        }

        override fun onDisconnectSocketConnection() {
            initRuntimeIfNeeded()
            socketConnection.connectionListener.onDisconnectSocketConnection()
        }

        override fun onReconnectFailedSocketConnectionWithError(error: NSError) {
            initRuntimeIfNeeded()
            socketConnection.connectionListener.onReconnectFailedSocketConnection(error.toException())
        }

        override fun onReconnectSuccessSocketConnectionWithError(error: NSError) {
            initRuntimeIfNeeded()
            socketConnection.connectionListener.onReconnectSuccessSocketConnection(error.toException())
        }

        override fun onThreadStartSocketConnection() {
            initRuntimeIfNeeded()
            socketConnection.connectionListener.onThreadStartSocketConnection()
        }

        override fun onResponseWithCount(count: Long, response: NSData) {
            initRuntimeIfNeeded()
            log("SocketConnectionDelegate.onResponseWithCount #0")
            val readBuffer = response.toByteArray()
            log("SocketConnectionDelegate.onResponseWithCount #1")
            dataBuffer.pushAll(readBuffer, 0, count.toInt())
            log("SocketConnectionDelegate.onResponseWithCount #2")
            socketConnection.responseListener.onResponse(dataBuffer)
            log("SocketConnectionDelegate.onResponseWithCount #3")
        }

        override fun onResetSocket() {
            initRuntimeIfNeeded()
            dataBuffer.clear()
        }
    }

    private val service = SocketConnectionService(
        socketConnectionProtocol,
        socketConnection.config.ip,
        NSNumber(socketConnection.config.port),
        NSNumber(socketConnection.config.responseTimeout.toDouble()),
        NSNumber(socketConnection.config.retriesCount.toDouble()),
        NSNumber(socketConnection.config.retryTimeout.toDouble())
    )

    actual fun start() {
        initRuntimeIfNeeded()
        service.start()
    }

    actual fun connect() {
        initRuntimeIfNeeded()
        service.connect()
    }

    actual fun disconnect() {
        initRuntimeIfNeeded()
        service.disconnect()
    }

    actual fun destroy() {
        initRuntimeIfNeeded()
        service.destroy()
    }

    actual fun write(bytes: ByteArray): Boolean {
        initRuntimeIfNeeded()
        return service.writeWithBytes(bytes.toNSData())
    }

    actual fun isConnected(): Boolean {
        initRuntimeIfNeeded()
        return service.isConnected()
    }

}
