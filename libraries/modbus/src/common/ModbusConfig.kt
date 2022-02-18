package featurea.modbus

import featurea.modbus.config.Channel
import featurea.modbus.config.Connection
import featurea.modbus.config.Directory
import featurea.modbus.config.Region
import featurea.modbus.support.IndexScope
import featurea.modbus.support.traverseNodes
import featurea.replaceVariable
import featurea.runtime.Component
import featurea.runtime.Constructor
import featurea.runtime.Module
import featurea.runtime.create
import featurea.sort

typealias ReadChannels = (channels: List<Channel>, readTime: Double) -> Unit

class ModbusConfig(override val module: Module) : Component {

    lateinit var filePath: String // quickfix todo avoid
    var cloudPlayerModbusId: String? = null
    val indexScope = IndexScope()
    lateinit var directory: Directory
    var readChannels: ReadChannels? = null

    private val connectionByChannelMap = mutableMapOf<Channel, Connection>()
    internal fun connectionOf(channel: Channel): Connection = connectionByChannelMap[channel]!!
    val channels: List<Channel> get() = connectionByChannelMap.keys.toList()
    private val channelMap = linkedMapOf<String, Channel>()
    val connections = linkedSetOf<Connection>()

    fun build() {
        // 1. inflate
        traverseNodes { currentConnection, currentChannel ->
            connections.add(currentConnection)
            connectionByChannelMap[currentChannel] = currentConnection
            if (currentChannel.isLocal) currentChannel.region = Region.Holdings // quickfix todo improve
            val region = currentChannel.region
            if (region != null) {
                currentConnection.channelByRegionMap.getOrPut(region) { linkedSetOf() }.add(currentChannel)
            }
            channelMap[currentChannel.name] = currentChannel
        }

        // 2. sort
        for (connection in connections) {
            for (channels in connection.channelByRegionMap.values) {
                channels.sort { it.address }
            }
        }

        // 3. index
        for ((name, channel) in channelMap) {
            indexScope.indexChannel(channel)
        }
    }

    fun findChannelOrNull(channelAliasOrName: String?): Channel? {
        @Suppress("NAME_SHADOWING")
        val channelAliasOrName: String = channelAliasOrName ?: return null
        val channelNameResolved: String = channelAliasOrName.replaceVariable("user", cloudPlayerModbusId)
        val channel: Channel? = channelMap[channelNameResolved]
        return channel
    }

}

@Constructor
fun Component.ModbusConfig(init: ModbusConfig.() -> Unit = {}) = create(init)
