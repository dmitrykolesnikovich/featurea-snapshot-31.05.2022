package featurea.modbus.master

import featurea.utils.getTimeMillis
import featurea.utils.log
import featurea.modbus.ModbusClient
import featurea.modbus.config.Channel
import featurea.modbus.config.Connection
import featurea.modbus.config.toSocketConnectionConfig
import featurea.modbus.support.MasterQuota
import featurea.modbus.support.MasterNotifier
import featurea.modbus.transaction.Transaction
import featurea.runtime.Component
import featurea.runtime.Constructor
import featurea.runtime.Module
import featurea.runtime.import
import featurea.socket.SocketConnection
import featurea.socket.SocketConnectionConfig
import featurea.socket.SocketConnectionListener

class Master(override val module: Module) : Component, SocketConnectionListener() {

    val master: Master = this
    val modbusClient: ModbusClient = import()

    lateinit var connection: Connection
    var socketConnection: SocketConnection? = null
    private val responseQueue = ResponseQueue(this)
    private val requestQueue = RequestQueue(this)
    var shouldRequest: Boolean = false
        internal set

    var hasConnection: Boolean = false
        private set
    var hasConnectionTime: Long = -1L
        private set

    var logger: Logger = modbusClient.logger
    val quota = MasterQuota(master)
    val notifier: MasterNotifier = MasterNotifier(master)
    var readRequestCount: Long = 0
    var writeRequestCount: Long = 0
    var reconnectionCount: Long = 0

    fun build() {
        val socketConnectionConfig = connection.toSocketConnectionConfig() ?: return
        socketConnection = SocketConnection(socketConnectionConfig).apply {
            listeners.connectionListener = master
            listeners.responseListener = responseQueue
        }
        quota.build()
    }

    fun create() {
        socketConnection?.start()
    }

    fun connect() {
        socketConnection?.connect()
    }

    fun disconnect() {
        socketConnection?.disconnect()
    }

    fun destroy() {
        socketConnection?.destroy()
    }

    override fun onThreadStartSocketConnection() {
        val ip = connection.ip ?: return
        logger.logThreadStart(ip, connection.port)
    }

    override fun onThreadStopSocketConnection() {
        val ip = connection.ip ?: return
        logger.logThreadStop(ip, connection.port)
    }

    override fun onConnectSuccessSocketConnection() {
        val socketConnection = socketConnection ?: return
        hasConnection = true
        hasConnectionTime = getTimeMillis().toLong()
        readRequestCount = 0
        writeRequestCount = 0
        shouldRequest = true
        val config = socketConnection.config
        logger.logConnect(config.ip, config.port)
    }

    override fun onDisconnectSocketConnection() {
        val socketConnection = socketConnection ?: return
        val config = socketConnection.config
        logger.logDisconnect(config.ip, config.port)
    }

    override fun onConnectFailedSocketConnection(e: Throwable) {
        val socketConnection = socketConnection ?: return
        hasConnection = false
        val config = socketConnection.config
        logger.logConnectionFailed(config.ip, config.port, e)
    }

    override fun onReconnectSuccessSocketConnection(e: Throwable) {
        val socketConnection = socketConnection ?: return
        val config = socketConnection.config
        log("($this) (${config.ip}:${config.port}) [Master.kt] reconnect: success")
        reset()
        readRequestCount = 0
        writeRequestCount = 0
        reconnectionCount++
        logger.onReconnectSuccessSocketConnection(config.ip, config.port, config.responseTimeout, e)
    }

    override fun onReconnectFailedSocketConnection(e: Throwable) {
        val socketConnection: SocketConnection = socketConnection ?: return
        val config: SocketConnectionConfig = socketConnection.config
        log("($this) (${config.ip}:${config.port}) [Master.kt] reconnect: failure")
        reset()
        logger.onReconnectFailedSocketConnection(config.ip, config.port, config.responseTimeout, e)
    }

    fun beginTransactionWriteChannelValue(channel: Channel, text: ShortArray) {
        requestQueue.produceWriteRequest(channel, text)
    }

    fun beginTransactionWriteChannelValue(channel: Channel, value: Double) {
        requestQueue.produceWriteRequest(channel, value)
    }

    fun commitTransaction(transactionId: Short): Transaction {
        return requestQueue.completeCurrentTransaction(transactionId)
    }

    fun requestTransactionFromQueue() {
        requestQueue.requestTransactionFromQueue()
    }

    fun updateQueues(elapsedTime: Float) {
        requestQueue.produceReadRequests(elapsedTime)
        responseQueue.consumeResponses()
    }

    override fun toString(): String = socketConnection?.config?.path ?: "undefined"

    /*internals*/

    internal var index: Int = -1

    private fun reset() {
        requestQueue.reset()
        responseQueue.reset()
        quota.reset()
    }

}

@Constructor
fun Component.Master(init: Master.() -> Unit = {}) = Master(module).apply { init(); build() }
