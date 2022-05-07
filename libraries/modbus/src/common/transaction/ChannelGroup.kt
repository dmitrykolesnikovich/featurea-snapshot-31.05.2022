package featurea.modbus.transaction

import featurea.utils.getTimeMillis
import featurea.modbus.config.Channel
import featurea.modbus.config.Connection
import featurea.modbus.config.Region
import featurea.modbus.config.Region.Holdings
import featurea.modbus.master.Logger
import featurea.modbus.support.IndexScope
import featurea.modbus.support.toFloat

@OptIn(ExperimentalStdlibApi::class)
class ChannelGroup(val connection: Connection, val region: Region, val channels: List<Channel>) {

    val startAddress: Short
    val finishAddress: Short
    var updateInterval: Float = 0f
    var updateProgress: Float = 0f
    var shouldRead: Boolean = false
    val size: Short get() = (finishAddress - startAddress + 1).toShort()
    var isVirtual: Boolean = false
        private set

    constructor(connection: Connection, channels: MutableList<Channel>) : this(connection, Holdings, channels) {
        this.isVirtual = true
    }

    init {
        startAddress = channels.first().startAddress
        finishAddress = channels.last().finishAddress
    }

    fun IndexScope.applyResponseVirtual(logger: Logger, notificationBuilder: StringBuilder? = null) {
        for (channel in channels) {
            channel.formulaService.applyReadValueWithoutReadFormula(channel.writeValueWithWriteFormula)
            if (channel.formulaService.hasReadFormulaError) {
                logger.logReadFormulaEvaluationError(channel.readFormula!!.value)
            }
            if (!channel.isLocal) { // quickfix todo improve
                notificationBuilder?.append("${channel.name}=${channel.readValueWithReadFormula},")
            }
        }
        // >> IMPORTANT order matters
        connection.config.readChannels?.invoke(channels, getTimeMillis())
        for (channel in channels) {
            channel.isReadValueWithReadFormulaValid = true
        }
        // <<
    }

    fun IndexScope.applyResponse(logger: Logger, transaction: Transaction, notificationBuilder: StringBuilder? = null) {
        val response: Response = transaction.response
        val minAddress = channels.first().startAddress
        for (channel in channels) {
            val from: Int = channel.startAddress - minAddress
            val to: Int = from + channel.registerCount
            val shortArray: ShortArray = checkNotNull(response.values).sliceArray(from until to)
            if (channel.hasDiapason) {
                channel.readValueInDiapason = shortArray
            } else {
                val value: Double = shortArray.toFloat(channel.type).toDouble()
                channel.formulaService.applyReadValueWithoutReadFormula(value)
                if (channel.formulaService.hasReadFormulaError) {
                    logger.logReadFormulaEvaluationError(channel.readFormula!!.value)
                }
                if (!channel.isLocal) { // quickfix todo improve
                    notificationBuilder?.append("${channel.name}=${channel.readValueWithReadFormula},")
                }
            }
        }
        // >> IMPORTANT order matters
        connection.config.readChannels?.invoke(channels, transaction.startTime.toDouble())
        for (channel in channels) {
            channel.isReadValueWithReadFormulaValid = true
        }
        // <<
    }

    fun onTick(elapsedTime: Float) {
        updateProgress += elapsedTime
        shouldRead = updateProgress >= updateInterval
        if (shouldRead) {
            updateProgress = 0f
        }
    }

}
