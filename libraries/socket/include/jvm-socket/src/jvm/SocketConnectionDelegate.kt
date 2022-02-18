package featurea.jvm.socket

import featurea.ByteQueue
import java.io.IOException
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketException
import java.net.SocketTimeoutException
import java.util.concurrent.atomic.AtomicInteger

class SocketConnectionConfig {
    var path: String = ""
    var ip: String = ""
    var port: Int = -1
    var retriesCount: Int = -1
    var responseTimeout: Int = -1
    var retryTimeout: Int = -1
    var isTcpNoDelay: Boolean = true // https://stackoverflow.com/a/34780678/909169

    override fun toString(): String =
        "SocketConnectionConfig(path='$path', ip='$ip', port=$port, retriesCount=$retriesCount, responseTimeout=$responseTimeout, retryTimeout=$retryTimeout, isTcpNoDelay=$isTcpNoDelay)"

}

open class SocketConnectionListener {
    open fun onConnectSuccessSocketConnection() {}
    open fun onConnectFailedSocketConnection(e: Throwable) {}
    open fun onReconnectSuccessSocketConnection(e: Throwable) {}
    open fun onReconnectFailedSocketConnection(e: Throwable) {}
    open fun onDisconnectSocketConnection() {}
    open fun onThreadStartSocketConnection() {}
    open fun onThreadStopSocketConnection() {}
}

open class SocketConnectionResponseListener {
    open fun onResponse(byteQueue: ByteQueue) {}
}

class SocketConnectionDelegateListeners {
    var config: SocketConnectionConfig = SocketConnectionConfig()
    var responseListener: SocketConnectionResponseListener = SocketConnectionResponseListener()
    var connectionListener: SocketConnectionListener = SocketConnectionListener()
}

// IMPORTANT https://stackoverflow.com/a/23588147
class SocketConnectionDelegate(val socketConnection: SocketConnectionDelegateListeners) : Runnable {

    private val runnable: Runnable = this
    private val dataBuffer = ByteQueue()
    private val readBuffer = ByteArray(1024)
    private var socket: Socket? = null
    var isDestroyed = false
        private set
    var isCreated = false
        private set
    private var hasDisconnect = false
    private val connectRequestCount = AtomicInteger(0)
    private var currentRetriesCount = 0
    private var hasRetries = true
    private val lockObject = Object()
    private val thread: Thread = Thread(runnable)
    private val ipAndPort: String get() = "${socketConnection.config.ip}:${socketConnection.config.port}"

    @Synchronized
    fun start() {
        thread.start()
        isCreated = true
        socketConnection.connectionListener.onThreadStartSocketConnection()
    }

    override fun run() {
        while (!isDestroyed) {
            if (connectRequestCount.get() > 0) {
                connectRequestCount.decrementAndGet()
                newSocket()
            } else if (isConnected()) {
                read()
            } else {
                log("($this) ($ipAndPort) [SocketConnectionDelegate.kt] wait: start")
                synchronized(lockObject) {
                    log("($this) ($ipAndPort) [SocketConnectionDelegate.kt] wait: synchronized")
                    while (!isDestroyed && connectRequestCount.get() <= 0) {
                        connectRequestCount.set(0)
                        try {
                            lockObject.wait()
                        } catch (e: InterruptedException) {
                            log("($this) ($ipAndPort) [SocketConnectionDelegate.kt] [ERROR] wait: ${e.localizedMessage}")
                        }
                    }
                }
                log("($this) ($ipAndPort) [SocketConnectionDelegate.kt] wait: complete")
            }
        }
        socketConnection.connectionListener.onThreadStopSocketConnection()
    }

    private fun read() {
        val socket: Socket = socket ?: error("socket: null")
        log("($this) ($ipAndPort) [SocketConnectionDelegate.kt] read: $socket (entering)")
        if (!isConnected()) {
            return
        }
        var count: Int = 0
        var exception: Throwable? = null
        try {
            val inputStream = socket.getInputStream()
            log("($this) ($ipAndPort) [SocketConnectionDelegate.kt] read: $socket (while)")
            while (inputStream.read(readBuffer).also { count = it } != -1) {
                dataBuffer.pushAll(readBuffer, 0, count)
                socketConnection.responseListener.onResponse(dataBuffer)
            }
        } catch (e: Throwable) {
            exception = e
        }
        if (exception != null) {
            reconnect(socket, exception)
        } else if (count == -1) {
            reconnect(socket, SocketException("count == -1"))
        }
        log("($this) ($ipAndPort) [SocketConnectionDelegate.kt] read: complete ($count)")
    }

    private fun reconnect(socket: Socket?, exception: Throwable) {
        log("($this) ($ipAndPort) [SocketConnectionDelegate.kt] reconnect: $socket (entering)")
        synchronized(lockObject) {
            log("($this) ($ipAndPort) [SocketConnectionDelegate.kt] reconnect: synchronized")
            log("($this) ($ipAndPort) [SocketConnectionDelegate.kt] [ERROR] reconnect: ${exception.localizedMessage}")
            if (socket != null && socket !== this.socket) {
                return
            }
            if (!hasDisconnect) {
                log("($this) ($ipAndPort) [SocketConnectionDelegate.kt] reconnect: success")
                socketConnection.connectionListener.onReconnectSuccessSocketConnection(exception)
                connect()
            } else {
                log("($this) ($ipAndPort) [SocketConnectionDelegate.kt] reconnect: failure")
                socketConnection.connectionListener.onReconnectFailedSocketConnection(exception)
            }
        }
        log("($this) ($ipAndPort) [SocketConnectionDelegate.kt] reconnect: $socket (complete)")
    }

    private fun newSocket() {
        val config: SocketConnectionConfig = socketConnection.config
        log("($this) ($ipAndPort) [SocketConnectionDelegate.kt] newSocket: $config (start)")
        val time1 = System.nanoTime()
        try {
            val socket: Socket = Socket()
            socket.setPerformancePreferences(0, 1, 0)
            socket.tcpNoDelay = config.isTcpNoDelay
            socket.soTimeout = config.responseTimeout
            socket.connect(InetSocketAddress(config.ip, config.port), config.responseTimeout)
            setSocket(socket)
            log("($this) ($ipAndPort) [SocketConnectionDelegate.kt] newSocket: ${config.ip}:${config.port}")
            socketConnection.connectionListener.onConnectSuccessSocketConnection()
        } catch (e: Throwable) {
            val time2 = System.nanoTime()
            val deltaTime = (time2 - time1) / 1000000
            val sleepTime = config.retryTimeout - deltaTime
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime)
                } catch (e1: InterruptedException) {
                    e1.printStackTrace()
                }
            }
            socketConnection.connectionListener.onConnectFailedSocketConnection(e)
            reconnect(socket, e)
        }
    }

    fun connect() {
        log("($this) ($ipAndPort) [SocketConnectionDelegate.kt] connect: start")
        hasDisconnect = false // by design
        setSocket(null)
        currentRetriesCount++
        if (currentRetriesCount > socketConnection.config.retriesCount) {
            hasRetries = false
            return
        }
        synchronized(lockObject) {
            connectRequestCount.incrementAndGet()
            lockObject.notify()
        }
        log("($this) ($ipAndPort) [SocketConnectionDelegate.kt] connect: complete")
    }

    fun destroy() {
        log("($this) ($ipAndPort) [SocketConnectionDelegate.kt] destroy: $isDestroyed (start)")
        if (isDestroyed) {
            return
        }
        isDestroyed = true
        synchronized(lockObject) {
            log("($this) ($ipAndPort) [SocketConnectionDelegate.kt] destroy: synchronized")
            lockObject.notify()
        }
        log("($this) ($ipAndPort) [SocketConnectionDelegate.kt] destroy: complete")
    }

    fun write(bytes: ByteArray): Boolean {
        // log("($this) ($ipAndPort) [SocketConnectionDelegate.kt] write: $socket (start)")
        val socket: Socket = socket ?: error("socket: null")
        return if (!isConnected()) {
            false
        } else try {
            val outputStream: OutputStream = socket.getOutputStream()
            outputStream.write(bytes)
            outputStream.flush()
            // log("($this) ($ipAndPort) [SocketConnectionDelegate.kt] write: $socket (complete)")
            true
        } catch (e: SocketException) {
            reconnect(socket, e)
            false
        } catch (e: SocketTimeoutException) {
            reconnect(socket, e)
            false
        } catch (e: Throwable) {
            log("($this) ($ipAndPort) [SocketConnectionDelegate.kt] [ERROR] write: ${e.localizedMessage}")
            false
        }
    }

    fun isConnected(): Boolean {
        val socket: Socket? = socket
        val result = socket != null && socket.isConnected && socket.isBound && !socket.isClosed
        return result
    }

    private fun setSocket(socket: Socket?) {
        log("($this) ($ipAndPort) [SocketConnectionDelegate.kt] setSocket: $socket (start)")
        synchronized(lockObject) {
            log("($this) ($ipAndPort) [SocketConnectionDelegate.kt] setSocket: synchronized")
            try {
                if (this.socket != null) {
                    this.socket!!.close()
                    this.socket = null
                    socketConnection.connectionListener.onDisconnectSocketConnection()
                    log("($this) ($ipAndPort) [SocketConnectionDelegate.kt] setSocket: onDisconnectSocketConnection")
                }
            } catch (e: IOException) {
                log("($this) ($ipAndPort) [SocketConnectionDelegate.kt] [ERROR] setSocket: ${e.localizedMessage}")
            }
            this.socket = socket
            if (this.socket != null) {
                currentRetriesCount = 0
            }
            dataBuffer.clear()
        }
        log("($this) ($ipAndPort) [SocketConnectionDelegate.kt] setSocket: $socket (complete)")
    }

    fun disconnect() {
        log("($this) ($ipAndPort) [SocketConnectionDelegate.kt] disconnect: start")
        hasDisconnect = true
        setSocket(null)
        log("($this) ($ipAndPort) [SocketConnectionDelegate.kt] disconnect: complete")
    }

    override fun toString(): String = socketConnection.config.path

    private fun log(message: String) {
        if (!message.contains("entering")) return // filter for now todo delete this
        featurea.log(message)
    }

}
