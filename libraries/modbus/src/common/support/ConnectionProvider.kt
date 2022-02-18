package featurea.modbus.support

import featurea.modbus.ModbusClient
import featurea.modbus.ModbusClientListener
import featurea.modbus.ModbusConfig
import featurea.modbus.config.Channel
import featurea.modbus.config.Connection
import featurea.modbus.master.Master
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import kotlin.math.min

class ConnectionProvider(override val module: Module) : Component {

    private val modbusClient: ModbusClient = import()

    var connectionLimit: Int = -1
    val hasConnectionLimit: Boolean get() = connectionLimit != -1
    var channelLimit: Int = -1
    val hasChannelLimit: Boolean get() = channelLimit != -1
    var actualConnectionCount: Int = 0
        private set
    var actualChannelCount: Int = 0
        private set

    override fun onCreateComponent() {
        modbusClient.listeners.add(ModbusClientListener { elapsedTime ->
            modbusClient.config.indexScope {
                for (master in modbusClient.masters) {
                    for ((_, quotaChannel) in master.quotaChannels) {
                        for (listener in modbusClient.listeners) {
                            listener.onTickChannel(quotaChannel, elapsedTime, master.notifier)
                        }
                    }
                }
            }
        })
    }

    fun forEachConnectionWithChannelLimit(config: ModbusConfig, action: (Connection, channelLimit: Int) -> Unit) {
        actualConnectionCount =
            if (hasConnectionLimit) min(config.connections.size, connectionLimit) else config.connections.size
        val connections = config.connections.toList().slice(0 until actualConnectionCount)
        if (hasChannelLimit) {
            var currentLimit = channelLimit
            for (connection in connections) {
                val channelCountNext = min(currentLimit, connection.channelCount)
                actualChannelCount += channelCountNext
                if (channelCountNext != 0) action(connection, channelCountNext)
                currentLimit -= channelCountNext
            }
        } else {
            for (connection in connections) {
                action(connection, connection.channelCount)
            }
        }
    }

}

class MasterQuota(val master: Master) {

    val indexScope: IndexScope get() = master.modbusClient.config.indexScope
    var channelLimit: Int = 0
    var quotaChannels = mutableMapOf<String, Channel>()

    fun build() {
        val result = mutableMapOf<String, Channel>()
        var counter: Int = 0



        // 2. result
        for (channels in master.connection.channelByRegionMap.values) {
            if (counter >= channelLimit) break
            for (channel in channels) {
                if (channel.isLocal) continue
                if (counter >= channelLimit) break
                result[channel.name] = channel
                counter++
            }
        }
        quotaChannels = result
    }

    fun reset() {
        reset(quotaChannels.values)
    }

    /*internals*/

    private fun reset(channels: Collection<Channel>) = indexScope {
        for (channel in channels) {
            val journalService = channel.journalService
            channel.isReadValueWithReadFormulaValid = false
            journalService.hasJournalValue = false
        }
    }

}
