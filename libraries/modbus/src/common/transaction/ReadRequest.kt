package featurea.modbus.transaction

import featurea.modbus.config.Channel
import featurea.modbus.config.Connection
import featurea.modbus.config.Region

fun ChannelGroup.createReadRequest(transactionId: Short): Request {
    return when (region) {
        Region.Coils -> Request.readCoils(transactionId, startAddress, size)
        Region.Discretes -> Request.readDiscretes(transactionId, startAddress, size)
        Region.Inputs -> Request.readInputs(transactionId, startAddress, size)
        Region.Holdings -> Request.readHoldings(transactionId, startAddress, size)
    }
}

fun Connection.updateChannelGroups(result: MutableList<ChannelGroup>, elapsedTime: Float, channelLimit: Int) {
    val virtualChannelGroup = mutableListOf<Channel>()
    var channelCounter: Int = 0
    fun isDone(): Boolean = channelCounter >= channelLimit
    for (channels in channelByRegionMap.values) {
        for (channel in channels) {
            channel.updateProgress(elapsedTime)
        }
    }
    for ((region, channels) in channelByRegionMap) {
        if (isDone()) break

        val channelGroup = mutableListOf<Channel>()
        var maxAddress: Int = -1

        fun appendChannelGroup() {
            if (channelGroup.isEmpty()) return
            result.add(ChannelGroup(this, region, ArrayList(channelGroup)))
            channelGroup.clear()
            maxAddress = -1
        }

        for (channel in channels) {
            if (isDone()) break
            channelCounter++

            if (channel.shouldRead) {
                if (channel.isVirtual) {
                    virtualChannelGroup.add(channel)
                } else {
                    if (channelGroup.isEmpty()) {
                        maxAddress = channel.startAddress + registerCount
                        channelGroup.add(channel)
                    } else if (channel.finishAddress < maxAddress) {
                        channelGroup.add(channel)
                    } else {
                        appendChannelGroup()
                        // >> quickfix todo improve
                        maxAddress = channel.startAddress + registerCount
                        channelGroup.add(channel)
                        // <<
                    }
                }
            }
        }

        appendChannelGroup()
    }

    if (virtualChannelGroup.isNotEmpty()) result.add(0, ChannelGroup(this, virtualChannelGroup)) // IMPORTANT goes first
}
