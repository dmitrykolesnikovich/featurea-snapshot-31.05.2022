package featurea.modbus

import featurea.modbus.config.Channel
import featurea.modbus.config.Connection
import featurea.modbus.master.Logger
import featurea.modbus.master.Master
import featurea.modbus.support.ConnectionProvider
import featurea.runtime.*

class ModbusClient constructor(override val module: Module) : Component {

    var isEnable: Boolean = true
    lateinit var config: ModbusConfig
        private set
    private val masterMap = mutableMapOf<Connection, Master>()
    private lateinit var state: ModbusClientState
    val logger = Logger()
    val listeners = mutableListOf<ModbusClientListener>()
    val masters: Collection<Master> get() = masterMap.values
    val isConnect: Boolean get() = state == ModbusClientState.CONNECT
    val isDestroy: Boolean get() = state == ModbusClientState.DESTROY

    // import
    val connectionProvider: ConnectionProvider = import()

    fun findMasterOrNull(connection: Connection): Master? = masterMap[connection]

    fun init(config: ModbusConfig) {
        this.config = config
        for (listener in listeners) listener.onInit()
    }

    fun create(block: () -> Unit = {}) {
        connectionProvider.forEachConnectionWithChannelLimit(config) { connection, channelLimit ->
            val master = Master { this.connection = connection; this.quota.channelLimit = channelLimit }
            masterMap[connection] = master
            master.create()
            config.indexScope.indexMaster(master)
        }
        state = ModbusClientState.INIT
        block()
        for (listener in listeners) listener.onCreate()
    }

    suspend fun connect(block: suspend () -> Unit = {}) {
        if (state == ModbusClientState.CONNECT) return
        for ((_, master) in masterMap) master.connect()
        state = ModbusClientState.CONNECT
        // log("[ModbusClient] #0")
        block()
        // log("[ModbusClient] #1")
        for (listener in listeners) {
            listener.onConnect()
        }
        // log("[ModbusClient] #2")
    }

    fun disconnect() {
        // log("[ModbusClient] disconnect: #0")
        if (state != ModbusClientState.CONNECT) return
        // log("[ModbusClient] disconnect: #1")
        for ((_, master) in masterMap) {
            // log("[ModbusClient] disconnect: #2")
            master.disconnect()
            // log("[ModbusClient] disconnect: #3")
        }
        // log("[ModbusClient] disconnect: #4")
        state = ModbusClientState.DISCONNECT
        // log("[ModbusClient] disconnect: #5")
        for (listener in listeners) {
            // log("[ModbusClient] disconnect: #6")
            listener.onDisconnect()
            // log("[ModbusClient] disconnect: #7")
        }
        // log("[ModbusClient] disconnect: #8")
    }

    fun destroy() {
        // log("[ModbusClient] destroy: #0")
        if (state == ModbusClientState.DESTROY) return
        // log("[ModbusClient] destroy: #1")
        state = ModbusClientState.DESTROY
        // log("[ModbusClient] destroy: #2")
        for ((_, master) in masterMap) {
            // log("[ModbusClient] destroy: #3")
            master.destroy()
            // log("[ModbusClient] destroy: #4")
        }
        // log("[ModbusClient] destroy: #5")
        for (listener in listeners) {
            // log("[ModbusClient] destroy: #6")
            listener.onDestroy()
            // log("[ModbusClient] destroy: #7")
        }
        // log("[ModbusClient] destroy: #8")
    }

    fun onTick(elapsedTime: Float) {
        requestTransactionFromQueue()
        updateQueues(elapsedTime)
    }

    fun requestTransactionFromQueue() {
        if (!isEnable) return
        if (isDestroy) return
        for (master in masters) {
            master.requestTransactionFromQueue()
        }
    }

    fun updateQueues(elapsedTime: Float) {
        if (!isEnable) return
        if (isDestroy) return
        for (master in masters) {
            master.updateQueues(elapsedTime)
        }
        for (listener in listeners) {
            listener.onTick(elapsedTime)
        }
    }

}

fun DependencyBuilder.ModbusClientPlugin(plugin: Plugin<ModbusClient>) = install(plugin)

fun ModbusClient.findQuotaChannels(): List<Channel> {
    val result = mutableListOf<Channel>()
    config.indexScope {
        for (master in masters) {
            for ((_, quotaChannel) in master.quotaChannels) {
                result.add(quotaChannel)
            }
        }
    }
    return result
}

fun ModbusConfig.readChannelsWithReadFormula(channels: String, readValueWithReadFormula: (Channel) -> Unit) {
    val tokens: List<String> = channels.split(",")
    for (token in tokens) {
        if (token.isBlank()) continue
        val (channelName, value) = token.split("=")

        val channel: Channel = findChannelOrNull(channelName) ?: continue
        val doubleValue = value.toDouble()
        channel.readValueWithReadFormula = doubleValue
        channel.isReadValueWithReadFormulaValid = true
        readValueWithReadFormula(channel)
    }
}

val ModbusClient.shouldRequest: Boolean get() = masters.count { it.shouldRequest } != 0
